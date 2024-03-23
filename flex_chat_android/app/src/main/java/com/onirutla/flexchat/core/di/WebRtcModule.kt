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

package com.onirutla.flexchat.di

import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.Logging
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object WebRtcModule {

    @Provides
    fun provideEglBaseContext(): EglBase.Context = EglBase.create().eglBaseContext

    @Provides
    fun provideVideoDecoderFactory(eglBaseContext: EglBase.Context): DefaultVideoDecoderFactory =
        DefaultVideoDecoderFactory(eglBaseContext)

    @Provides
    fun provideVideoEncoderFactory(eglBaseContext: EglBase.Context): DefaultVideoEncoderFactory =
        DefaultVideoEncoderFactory(eglBaseContext, true, true)

    @Provides
    fun provideRTCConfiguration(): PeerConnection.RTCConfiguration {
        val stunServers = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302",
        )
        return PeerConnection.RTCConfiguration(
            listOf(
                PeerConnection.IceServer.builder(stunServers)
                    .createIceServer()
            )
        ).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
    }

    @Provides
    fun providePeerConnectionFactory(
        @ApplicationContext context: Context,
        videoEncoderFactory: DefaultVideoEncoderFactory,
        videoDecoderFactory: DefaultVideoDecoderFactory,
    ): PeerConnectionFactory {
        val peerConnectionOption = PeerConnectionFactory.InitializationOptions.builder(context)
            .setInjectableLogger({ message, severity, label ->
                when (severity) {
                    Logging.Severity.LS_VERBOSE -> {
                        Timber.v("[onLogMessage] label: $label, message: $message")
                    }

                    Logging.Severity.LS_INFO -> {
                        Timber.i("[onLogMessage] label: $label, message: $message")
                    }

                    Logging.Severity.LS_WARNING -> {
                        Timber.w("[onLogMessage] label: $label, message: $message")
                    }

                    Logging.Severity.LS_ERROR -> {
                        Timber.e("[onLogMessage] label: $label, message: $message")
                    }

                    Logging.Severity.LS_NONE -> {
                        Timber.d("[onLogMessage] label: $label, message: $message")
                    }

                    else -> {}
                }
            }, Logging.Severity.LS_VERBOSE)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(peerConnectionOption)

        val javaAudioDeviceModule = JavaAudioDeviceModule
            .builder(context)
            .setUseHardwareAcousticEchoCanceler(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            .setUseHardwareNoiseSuppressor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            .setAudioRecordErrorCallback(object :
                JavaAudioDeviceModule.AudioRecordErrorCallback {
                override fun onWebRtcAudioRecordInitError(p0: String?) {
                    Timber.w("[onWebRtcAudioRecordInitError] $p0")
                }

                override fun onWebRtcAudioRecordStartError(
                    p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                    p1: String?,
                ) {
                    Timber.w("[onWebRtcAudioRecordInitError] $p1")
                }

                override fun onWebRtcAudioRecordError(p0: String?) {
                    Timber.w("[onWebRtcAudioRecordError] $p0")
                }
            })
            .setAudioTrackErrorCallback(object :
                JavaAudioDeviceModule.AudioTrackErrorCallback {
                override fun onWebRtcAudioTrackInitError(p0: String?) {
                    Timber.w("[onWebRtcAudioTrackInitError] $p0")
                }

                override fun onWebRtcAudioTrackStartError(
                    p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                    p1: String?,
                ) {
                    Timber.w("[onWebRtcAudioTrackStartError] $p0")
                }

                override fun onWebRtcAudioTrackError(p0: String?) {
                    Timber.w("[onWebRtcAudioTrackError] $p0")
                }
            })
            .setAudioRecordStateCallback(object :
                JavaAudioDeviceModule.AudioRecordStateCallback {
                override fun onWebRtcAudioRecordStart() {
                    Timber.d("[onWebRtcAudioRecordStart] no args")
                }

                override fun onWebRtcAudioRecordStop() {
                    Timber.d("[onWebRtcAudioRecordStop] no args")
                }
            })
            .setAudioTrackStateCallback(object :
                JavaAudioDeviceModule.AudioTrackStateCallback {
                override fun onWebRtcAudioTrackStart() {
                    Timber.d("[onWebRtcAudioTrackStart] no args")
                }

                override fun onWebRtcAudioTrackStop() {
                    Timber.d("[onWebRtcAudioTrackStop] no args")
                }
            })
            .createAudioDeviceModule().also {
                it.setMicrophoneMute(false)
                it.setSpeakerMute(false)
            }

        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setAudioDeviceModule(javaAudioDeviceModule)
            .createPeerConnectionFactory()
    }
}
