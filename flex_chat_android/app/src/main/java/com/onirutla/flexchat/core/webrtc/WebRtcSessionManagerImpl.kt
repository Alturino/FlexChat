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

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.webrtc.Camera2Capturer
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerationAndroid
import org.webrtc.MediaConstraints
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.util.UUID

class WebRtcSessionManagerImpl(
    private val context: Context,
    override val peerConnectionFactory: FlexChatPeerConnectionFactory,
    override val signalingClient: SignalingClient,
) : WebRtcSessionManager {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _localVideoTrackFlow = MutableSharedFlow<VideoTrack>()
    override val localVideoTrackFlow: SharedFlow<VideoTrack> = _localVideoTrackFlow.asSharedFlow()

    private val _remoteVideoTrackFlow = MutableSharedFlow<VideoTrack>()
    override val remoteVideoTrackFlow: SharedFlow<VideoTrack>
        get() = _remoteVideoTrackFlow.asSharedFlow()

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.addAll(
            listOf(
                MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"),
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
        )
    }

    // getting front camera
    private val videoCapturer: VideoCapturer by lazy { buildCameraCapturer() }
    private val cameraManager by lazy { context.getSystemService<CameraManager>() }
    private val cameraEnumerator: Camera2Enumerator by lazy {
        Camera2Enumerator(context)
    }

    private val resolution: CameraEnumerationAndroid.CaptureFormat
        get() {
            val frontCamera = cameraEnumerator.deviceNames.first { cameraName ->
                cameraEnumerator.isFrontFacing(cameraName)
            }
            val supportedFormats = cameraEnumerator.getSupportedFormats(frontCamera) ?: listOf()
            return supportedFormats.firstOrNull { it.width == 720 || it.width == 480 || it.width == 360 }
                ?: error("There is no matched resolution")
        }

    private val surfaceTextureHelper = SurfaceTextureHelper.create(
        "SurfaceTextureHelperThread",
        peerConnectionFactory.eglBaseContext
    )

    private val videoSource by lazy {
        peerConnectionFactory.makeVideoSource(videoCapturer.isScreencast).apply {
            videoCapturer.initialize(surfaceTextureHelper, context, capturerObserver)
            videoCapturer.startCapture(resolution.width, resolution.height, 30)
        }
    }

    private val localVideoTrack by lazy {
        peerConnectionFactory.makeVideoTrack(
            source = videoSource,
            trackId = "Video${UUID.randomUUID()}"
        )
    }

    private val audioHandler by lazy { AudioSwitchHandler(context) }

    private fun buildCameraCapturer(): VideoCapturer {
        val manager = cameraManager ?: throw RuntimeException("CameraManager was not initialized")
        val ids = manager.cameraIdList

        var foundCamera = false
        var cameraId = ""
        for (id in ids) {
            val characteristics = manager.getCameraCharacteristics(id)
            val cameraLensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (cameraLensFacing == CameraMetadata.LENS_FACING_FRONT) {
                foundCamera = true
                cameraId = id
            }
        }

        if (!foundCamera && ids.isNotEmpty()) {
            cameraId = ids.first()
        }

        return Camera2Capturer(context, cameraId, null)
    }

    override fun onSessionScreenReady() {
        TODO("Not yet implemented")
    }

    override fun flipCamera() {
        TODO("Not yet implemented")
    }

    override fun enableMicrophone(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun enableCamera(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun disconnect(enabled: Boolean) {
        TODO("Not yet implemented")
    }

}
