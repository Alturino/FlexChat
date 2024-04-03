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

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onirutla.flexchat.core.VideoRenderer
import com.onirutla.flexchat.core.ui.components.FloatingVideoRenderer
import com.onirutla.flexchat.core.webrtc.LocalWebRtcSessionManager


data class CallMediaState(
    val isMicrophoneEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true,
)

@Composable
fun VideoCallScreen(modifier: Modifier = Modifier) {
    val sessionManager = LocalWebRtcSessionManager.current
    LaunchedEffect(key1 = Unit) {
        sessionManager.onSessionScreenReady()
    }

    var parentSize by remember { mutableStateOf(IntSize(0, 0)) }
    val remoteVideoTrackState by sessionManager.remoteVideoTrackFlow
        .collectAsStateWithLifecycle(null)
    val remoteVideoTrack = remoteVideoTrackState

    val localVideoTrackState by sessionManager.localVideoTrackFlow
        .collectAsStateWithLifecycle(null)
    val localVideoTrack = localVideoTrackState

    var callMediaState by remember { mutableStateOf(CallMediaState()) }


    val activity = (LocalContext.current as? Activity)

    Box(modifier = modifier.fillMaxSize()) {
        if (remoteVideoTrack != null) {
            VideoRenderer(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { parentSize = it },
                videoTrack = remoteVideoTrack
            )
        }
        if (localVideoTrack != null && callMediaState.isCameraEnabled) {
            FloatingVideoRenderer(
                modifier = Modifier
                    .size(width = 150.dp, height = 210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopEnd),
                videoTrack = localVideoTrack,
                parentBounds = parentSize,
                paddingValues = PaddingValues(8.dp)
            )
        }
    }
}


@Composable
fun VideoCallControls(
    modifier: Modifier,
    callMediaState: CallMediaState,
    actions: List<VideoCallControlAction> = buildDefaultCallControlActions(callMediaState = callMediaState),
    onCallAction: (CallAction) -> Unit,
) {
    LazyRow(
        modifier = modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(actions) { action ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(action.background)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.Center)
                        .clickable { onCallAction(action.callAction) },
                    tint = action.iconTint,
                    imageVector = action.icon,
                    contentDescription = null
                )
            }
        }
    }
}
