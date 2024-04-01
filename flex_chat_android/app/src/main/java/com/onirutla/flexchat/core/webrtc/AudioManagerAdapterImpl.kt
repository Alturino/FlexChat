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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import timber.log.Timber

class AudioManagerAdapterImpl(
    private val context: Context,
    private val audioManager: AudioManager,
    private val audioFocusRequest: AudioFocusRequestWrapper = AudioFocusRequestWrapper(),
    private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener,
) : AudioManagerAdapter {


    private var savedAudioMode = 0
    private var savedIsMicrophoneMuted = false
    private var savedSpeakerphoneEnabled = false
    private var audioRequest: AudioFocusRequest? = null

    init {
        Timber.i("<init> audioFocusChangeListener: $audioFocusChangeListener")
    }

    override fun hasEarpiece(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    @SuppressLint("NewApi")
    override fun hasSpeakerphone(): Boolean {
        return if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true
                }
            }
            false
        } else {
            true
        }
    }

    @SuppressLint("NewApi")
    override fun setAudioFocus() {
        // Request audio focus before making any device switch.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioRequest = audioFocusRequest.buildRequest(audioFocusChangeListener)
            audioRequest?.let {
                val result = audioManager.requestAudioFocus(it)
                Timber.i("[setAudioFocus] #new; completed: ${result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED}")
            }
        } else {
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
            Timber.i("[setAudioFocus] #old; completed: ${result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED}")
        }
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance. Some devices have difficulties with speaker mode
         * if this is not set.
         */
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    override fun enableBluetoothSco(enable: Boolean) {
        Timber.i("[enableBluetoothSco] enable: $enable")
        audioManager.run { if (enable) startBluetoothSco() else stopBluetoothSco() }
    }

    override fun enableSpeakerphone(enable: Boolean) {
        Timber.i("[enableSpeakerphone] enable: $enable")
        audioManager.isSpeakerphoneOn = enable
    }

    override fun mute(mute: Boolean) {
        Timber.i("[mute] mute: $mute")
        audioManager.isMicrophoneMute = mute
    }

    // TODO Consider persisting audio state in the event of process death
    override fun cacheAudioState() {
        Timber.i("[cacheAudioState] no args")
        savedAudioMode = audioManager.mode
        savedIsMicrophoneMuted = audioManager.isMicrophoneMute
        savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn
    }

    @SuppressLint("NewApi")
    override fun restoreAudioState() {
        Timber.i("[cacheAudioState] no args")
        audioManager.mode = savedAudioMode
        mute(savedIsMicrophoneMuted)
        enableSpeakerphone(savedSpeakerphoneEnabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioRequest?.let {
                Timber.d("[cacheAudioState] abandonAudioFocusRequest: $it")
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            Timber.d("[cacheAudioState] audioFocusChangeListener: $audioFocusChangeListener")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
}


class AudioFocusRequestWrapper {
    @SuppressLint("NewApi")
    fun buildRequest(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioFocusRequest {
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(playbackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }
}
