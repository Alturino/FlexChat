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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.conversation.ui.call.incoming_call.IncomingCallScreen
import com.onirutla.flexchat.conversation.ui.call.ongoing_call.OngoingCallScreen
import com.onirutla.flexchat.conversation.ui.call.ongoing_call.OngoingCallUiState
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.core.webrtc.WebRTCSessionState

@Composable
fun CallScreen(
    modifier: Modifier = Modifier,
    state: WebRTCSessionState,
    onCallFinished: () -> Unit,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.Top,
        ) {
            when (val callState = state.callState) {
                is CallState.Incoming -> {
                    IncomingCallScreen(onEvent = {}, callerName = callState.callerName)
                }

                is CallState.Outgoing -> {
                    OngoingCallScreen(state = OngoingCallUiState(), onEvent = {})
                }

                CallState.Initial -> {

                }

                CallState.None, CallState.UnRegistered -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Call ended", style = MaterialTheme.typography.titleLarge)
                    }
                }

            }
        }
    }
}

@Preview
@Composable
private fun OutgoingOngoingCallScreenPreview() {
    FlexChatTheme {
        CallScreen(state = WebRTCSessionState.Creating, onCallFinished = {})
    }
}

@Preview
@Composable
private fun IncomingCallScreenPreview() {
    FlexChatTheme {
        CallScreen(state = WebRTCSessionState.Active, onCallFinished = {})
    }
}
