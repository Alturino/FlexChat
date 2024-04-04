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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            when (state) {
                WebRTCSessionState.Active -> {}
                WebRTCSessionState.Creating -> {}
                WebRTCSessionState.Ready -> {}
                WebRTCSessionState.Impossible -> {}
                WebRTCSessionState.Offline -> {}
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
