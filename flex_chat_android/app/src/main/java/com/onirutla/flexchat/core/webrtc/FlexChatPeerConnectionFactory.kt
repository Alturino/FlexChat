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
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject

class FlexChatPeerConnectionFactory @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val videoDecoderFactory: DefaultVideoDecoderFactory,
    private val videoEncoderFactory: DefaultVideoEncoderFactory,
    val eglBaseContext: EglBase.Context,
    val rtcConfiguration: PeerConnection.RTCConfiguration,
) {
    fun makePeerConnection(
        configuration: PeerConnection.RTCConfiguration,
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
            streamPeerType = type,
        )
        val connection = makePeerConnectionInternal(
            configuration = configuration,
            observer = peerConnection
        )
        return peerConnection.apply { initialize(connection) }
    }

    private fun makePeerConnectionInternal(
        configuration: PeerConnection.RTCConfiguration,
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
