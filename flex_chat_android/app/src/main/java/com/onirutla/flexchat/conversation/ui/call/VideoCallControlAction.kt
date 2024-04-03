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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface CallAction {
    data class ToggleMicroPhone(val isEnabled: Boolean) : CallAction
    data class ToggleCamera(val isEnabled: Boolean) : CallAction
    data object FlipCamera : CallAction
    data object LeaveCall : CallAction
}

data class VideoCallControlAction(
    val icon: ImageVector,
    val iconTint: Color,
    val background: Color,
    val callAction: CallAction,
)

@Composable
fun buildDefaultCallControlActions(
    callMediaState: CallMediaState,
): List<VideoCallControlAction> {
    val microphoneIcon = if (callMediaState.isMicrophoneEnabled) {
        Icons.Default.Mic
    } else {
        Icons.Default.MicOff
    }
    val cameraIcon = if (callMediaState.isCameraEnabled) {
        Icons.Default.Videocam
    } else {
        Icons.Default.VideocamOff
    }

    return listOf(
        VideoCallControlAction(
            icon = microphoneIcon,
            iconTint = Color.White,
            background = MaterialTheme.colorScheme.primaryContainer,
            callAction = CallAction.ToggleMicroPhone(callMediaState.isMicrophoneEnabled)
        ),
        VideoCallControlAction(
            icon = cameraIcon,
            iconTint = Color.White,
            background = MaterialTheme.colorScheme.primaryContainer,
            callAction = CallAction.ToggleCamera(callMediaState.isCameraEnabled)
        ),
        VideoCallControlAction(
            icon = Icons.Default.FlipCameraAndroid,
            iconTint = Color.White,
            background = MaterialTheme.colorScheme.primaryContainer,
            callAction = CallAction.FlipCamera
        ),
        VideoCallControlAction(
            icon = Icons.Default.CallEnd,
            iconTint = Color.White,
            background = MaterialTheme.colorScheme.errorContainer,
            callAction = CallAction.LeaveCall
        )
    )
}
