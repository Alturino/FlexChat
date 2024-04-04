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

package com.onirutla.flexchat.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun InAppIncomingCallNotification(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
) {
    Surface(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Received incoming call")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledIconButton(onClick = onAccept) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null)
                }
                FilledIconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Red)
                ) {
                    Icon(imageVector = Icons.Default.CallEnd, contentDescription = null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun InAppIncomingCallNotificationPreview() {
    FlexChatTheme {
        InAppIncomingCallNotification(onAccept = { /*TODO*/ }, onReject = {})
    }
}
