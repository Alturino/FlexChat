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

package com.onirutla.flexchat.conversation.ui.call.ongoing_call

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Earbuds
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SpeakerPhone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun OngoingCallScreen(
    modifier: Modifier = Modifier,
    state: OngoingCallUiState,
    onEvent: (OngoingCallEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(
            left = 16.dp,
            top = 16.dp,
            right = 16.dp,
            bottom = 16.dp
        )
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Someone",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calling",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val audioOutputType = when (state.audioOutputType) {
                            AudioOutputType.Bluetooth -> AudioOutputType.Speaker
                            AudioOutputType.Speaker -> AudioOutputType.Earbuds
                            AudioOutputType.Earbuds -> AudioOutputType.Bluetooth
                        }
                        onEvent(OngoingCallEvent.OnAudioOutputChanged(audioOutputType))
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                ) {
                    when (state.audioOutputType) {
                        AudioOutputType.Bluetooth -> {
                            Icon(
                                imageVector = Icons.Rounded.BluetoothAudio,
                                contentDescription = null
                            )
                        }

                        AudioOutputType.Speaker -> {
                            Icon(
                                imageVector = Icons.Rounded.SpeakerPhone,
                                contentDescription = null
                            )
                        }

                        AudioOutputType.Earbuds -> {
                            Icon(imageVector = Icons.Rounded.Earbuds, contentDescription = null)
                        }
                    }
                }
                FilledTonalIconToggleButton(
                    checked = state.isCameraEnabled,
                    onCheckedChange = { onEvent(OngoingCallEvent.OnIsCameraEnabledChange(it)) },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                ) {
                    if (state.isCameraEnabled) {
                        Icon(imageVector = Icons.Rounded.Videocam, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Rounded.VideocamOff, contentDescription = null)
                    }
                }
                FilledTonalIconToggleButton(
                    checked = state.isMicEnabled,
                    onCheckedChange = { onEvent(OngoingCallEvent.OnMicEnabledChanged(it)) },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    when (state.isMicEnabled) {
                        true -> {
                            Icon(imageVector = Icons.Rounded.Mic, contentDescription = null)
                        }

                        false -> {
                            Icon(imageVector = Icons.Rounded.MicOff, contentDescription = null)
                        }
                    }
                }
                FilledTonalIconButton(
                    onClick = { onEvent(OngoingCallEvent.OnCallEnd) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.CallEnd, contentDescription = null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun OutgoingCallScreenPreview() {
    FlexChatTheme {
        OngoingCallScreen(state = OngoingCallUiState(), onEvent = {})
    }
}
