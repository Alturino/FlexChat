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

package com.onirutla.flexchat.conversation.ui.call

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.core.webrtc.FlexChatPeerConnectionFactory
import com.onirutla.flexchat.core.webrtc.LocalWebRtcSessionManager
import com.onirutla.flexchat.core.webrtc.SignalingClient
import com.onirutla.flexchat.core.webrtc.WebRtcSessionManager
import com.onirutla.flexchat.core.webrtc.WebRtcSessionManagerImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : ComponentActivity() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

        val conversationId = intent.getStringExtra("conversationId").orEmpty()
        val sessionManager: WebRtcSessionManager = WebRtcSessionManagerImpl(
            context = this,
            signalingClient = SignalingClient(firestore),
            conversationId = conversationId,
            peerConnectionFactory = FlexChatPeerConnectionFactory(this)
        )
        setContent {
            FlexChatTheme {
                CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager){
                    Surface {
                        val state by sessionManager.signalingClient.sessionStateFlow.collectAsStateWithLifecycle()
                        VideoCallScreen()
                    }
                }
            }
        }
    }
}
