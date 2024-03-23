/*
 * Copyright 2024 Ricky Alturino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onirutla.flexchat.core.webrtc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.IceCandidateErrorEvent
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import timber.log.Timber
import javax.inject.Inject

enum class StreamPeerType {
    Publisher,
    Subscriber,
}

class FlexChatPeerConnectionFactory @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val videoDecoderFactory: DefaultVideoDecoderFactory,
    private val videoEncoderFactory: DefaultVideoEncoderFactory,
    val eglBaseContext: EglBase.Context,
    val rtcConfiguration: RTCConfiguration,
) {
    fun makePeerConnection(
        configuration: RTCConfiguration,
        coroutineScope: CoroutineScope,
        mediaConstraints: MediaConstraints,
        onIceCandidateRequest: ((IceCandidate, StreamPeerType) -> Unit)? = null,
        onNegotiationNeeded: ((FlexChatPeerConnection, StreamPeerType) -> Unit)? = null,
        onStreamAdded: ((MediaStream) -> Unit)? = null,
        onVideoTrack: ((RtpTransceiver?) -> Unit)? = null,
        type: StreamPeerType,
    ): FlexChatPeerConnection {
        val peerConnection = FlexChatPeerConnection(
            coroutineScope = coroutineScope,
            mediaConstraints = mediaConstraints,
            onIceCandidate = onIceCandidateRequest,
            onNegotiationNeeded = onNegotiationNeeded,
            onStreamAdded = onStreamAdded,
            onVideoTrack = onVideoTrack,
            type = type,
        )
        val connection = makePeerConnectionInternal(
            configuration = configuration,
            observer = peerConnection
        )
        return peerConnection.apply { initialize(connection) }
    }

    private fun makePeerConnectionInternal(
        configuration: RTCConfiguration,
        observer: PeerConnection.Observer,
    ): PeerConnection = requireNotNull(
        peerConnectionFactory.createPeerConnection(configuration, observer)
    )

    fun makeVideoSource(isScreencast: Boolean): VideoSource = peerConnectionFactory
        .createVideoSource(isScreencast)

    fun makeVideoTrack(source: VideoSource, trackId: String): VideoTrack = peerConnectionFactory
        .createVideoTrack(trackId, source)

    fun makeAudioSource(
        constraints: MediaConstraints = MediaConstraints(),
    ): AudioSource = peerConnectionFactory
        .createAudioSource(constraints)

    fun makeAudioTrack(
        source: AudioSource,
        trackId: String,
    ): AudioTrack = peerConnectionFactory
        .createAudioTrack(trackId, source)
}

class FlexChatPeerConnection(
    private val coroutineScope: CoroutineScope,
    private val mediaConstraints: MediaConstraints,
    private val onIceCandidate: ((IceCandidate, StreamPeerType) -> Unit)?,
    private val onNegotiationNeeded: ((FlexChatPeerConnection, StreamPeerType) -> Unit)?,
    private val onStreamAdded: ((MediaStream) -> Unit)?,
    private val onVideoTrack: ((RtpTransceiver?) -> Unit)?,
    private val type: StreamPeerType,
) : PeerConnection.Observer {

    lateinit var peerConnection: PeerConnection
        private set

    private var statsJob: Job? = null

    private val pendingIceMutex: Mutex = Mutex()

    private val pendingIceCandidates: MutableList<IceCandidate> = mutableListOf()

    fun initialize(peerConnection: PeerConnection) {
        Timber.i("peerConnection: $peerConnection")
        this.peerConnection = peerConnection
    }

    suspend fun createOffer(): SessionDescription {
        Timber.d("Create Offer")
        return getSessionDescription { peerConnection.createOffer(it, mediaConstraints) }
    }

    suspend fun createAnswer(): SessionDescription {
        Timber.d("Create Answer")
        return getSessionDescription { peerConnection.createAnswer(it, mediaConstraints) }
    }

    suspend fun setRemoteDescription(
        sessionDescription: SessionDescription,
    ) = setSessionDescription {
        peerConnection.setRemoteDescription(
            it,
            SessionDescription(
                sessionDescription.type,
                sessionDescription.description.mungeCodecs()
            )
        )
    }.also {
        pendingIceMutex.withLock {
            pendingIceCandidates.forEach { iceCandidate ->
                Timber.i("setRemoteDescription pendingRtcIceCandidate: $iceCandidate")
                peerConnection.addRtcIceCandidate(iceCandidate)
            }
            pendingIceCandidates.clear()
        }
    }

    suspend fun setLocalDescription(
        sessionDescription: SessionDescription,
    ) = setSessionDescription {
        val sdp = with(sessionDescription) {
            SessionDescription(type, description)
        }
        peerConnection.setLocalDescription(it, sdp)
    }

    suspend fun addIceCandidate(iceCandidate: IceCandidate): Result<Unit> {
        if (peerConnection.remoteDescription == null) {
            Timber.w("Postponed (no remoteDescription)")
            pendingIceMutex.withLock {
                pendingIceCandidates.add(iceCandidate)
            }
            return Result.failure(RuntimeException("RemoteDescription is not set"))
        }
        Timber.d("rtcIceCandidate: $iceCandidate")
        return Result.success(peerConnection.addRtcIceCandidate(iceCandidate)).also {
            Timber.v("Completed: $it")
        }
    }

    override fun onIceCandidate(iceCandidate: IceCandidate?) {
        Timber.i("iceCandidate: $iceCandidate")
        if (iceCandidate == null)
            return
        onIceCandidate?.invoke(iceCandidate, StreamPeerType.Publisher)
    }


    override fun onAddStream(mediaStream: MediaStream?) {
        Timber.i("mediaStream: $mediaStream")
        mediaStream?.let { onStreamAdded?.invoke(it) }
    }

    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
        super.onAddTrack(receiver, mediaStreams)
        Timber.i("receiver: $receiver, mediaStreams: $mediaStreams")
        mediaStreams?.forEach { mediaStream ->
            Timber.v("mediaStream: $mediaStream")
            mediaStream.audioTracks.forEach { remoteAudioTrack ->
                Timber.v("remoteAudioTrack: $remoteAudioTrack")
                remoteAudioTrack.setEnabled(true)
            }
            onStreamAdded?.invoke(mediaStream)
        }
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {
        Timber.d(signalingState?.name.orEmpty())
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
        Timber.d(iceConnectionState?.name.orEmpty())
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        super.onStandardizedIceConnectionChange(newState)
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        TODO("Not yet implemented")
    }


    override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
        super.onIceCandidateError(event)
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        TODO("Not yet implemented")
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        super.onSelectedCandidatePairChanged(event)
    }


    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
        TODO("Not yet implemented")
    }

    override fun onRenegotiationNeeded() {
        TODO("Not yet implemented")
    }


    override fun onRemoveTrack(receiver: RtpReceiver?) {
        super.onRemoveTrack(receiver)
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
    }
}

private fun String.mungeCodecs() =
    replace("vp9", "VP9").replace("vp8", "VP8").replace("h264", "H264")
