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

import com.onirutla.flexchat.core.webrtc.util.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.IceCandidateErrorEvent
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RTCStatsReport
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class FlexChatPeerConnection(
    private val coroutineScope: CoroutineScope,
    private val mediaConstraints: MediaConstraints,
    private val onIceCandidate: ((IceCandidate, StreamPeerType) -> Unit)?,
    private val onNegotiationNeeded: ((FlexChatPeerConnection, StreamPeerType) -> Unit)?,
    private val onStreamAdded: ((MediaStream) -> Unit)?,
    private val onVideoTrack: ((RtpTransceiver?) -> Unit)?,
    private val streamPeerType: StreamPeerType,
) : PeerConnection.Observer {

    lateinit var peerConnection: PeerConnection
        private set

    private var statsJob: Job? = null

    private val pendingIceMutex: Mutex = Mutex()
    private val pendingIceCandidates: MutableList<IceCandidate> = mutableListOf()

    private val statsFlow: MutableStateFlow<RTCStatsReport?> = MutableStateFlow(null)

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
        Timber.d("setLocalDescription: offerSdp: ${sessionDescription.stringify()}")
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
        iceCandidate?.let { onIceCandidate?.invoke(it, streamPeerType) }
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
        Timber.d("onIceConnectionChange: iceConnectionState: $iceConnectionState")
        when (iceConnectionState) {
            PeerConnection.IceConnectionState.NEW -> {}
            PeerConnection.IceConnectionState.CHECKING -> {}
            PeerConnection.IceConnectionState.CONNECTED -> {
                statsJob = observeStats()
            }

            PeerConnection.IceConnectionState.COMPLETED -> {}
            PeerConnection.IceConnectionState.FAILED -> {}
            PeerConnection.IceConnectionState.DISCONNECTED -> {
                statsJob?.cancel()
            }

            PeerConnection.IceConnectionState.CLOSED -> {}
            null -> {}
        }
    }

    private fun observeStats() = coroutineScope.launch {
        while (isActive) {
            delay(10.seconds)
            peerConnection.getStats { rtcStatsReport ->
                Timber.v("observeStats: $rtcStatsReport")
                statsFlow.update { rtcStatsReport }
            }
        }
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Timber.i("onIceConnectionReceivingChange: receiving: $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Timber.i("onIceGatheringChange: newIceGatheringState: $p0")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        Timber.i("onConnectionChange: newState: $newState")
    }

    override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
        super.onIceCandidateError(event)
        Timber.i("onIceCandidateError: event: $event")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Timber.i("onIceCandidatesRemoved: iceCandidates: $p0")
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        super.onSelectedCandidatePairChanged(event)
        Timber.i("onSelectedCandidatePairChanged: event: $event")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Timber.i("onRemoveStream removed mediaStream: $p0")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Timber.i("onDataChannel: dataChannel: $p0")
    }

    override fun onRenegotiationNeeded() {
        Timber.i("onRenegotiationNeeded no args")
        onNegotiationNeeded?.invoke(this, streamPeerType)
    }


    override fun onRemoveTrack(receiver: RtpReceiver?) {
        super.onRemoveTrack(receiver)
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        Timber.i("onTrack: transceiver: $transceiver")
        onVideoTrack?.invoke(transceiver)
    }
}

private fun String.mungeCodecs() =
    replace("vp9", "VP9").replace("vp8", "VP8").replace("h264", "H264")
