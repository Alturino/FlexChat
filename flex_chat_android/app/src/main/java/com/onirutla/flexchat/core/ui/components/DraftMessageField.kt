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

package com.onirutla.flexchat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Attachment
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.InsertEmoticon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@Composable
fun DraftMessageField(
    modifier: Modifier = Modifier,
    message: String,
    onDraftMessageChanged: (String) -> Unit,
    onDraftMessageSend: (KeyboardActionScope.() -> Unit),
    onEmojiClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCameraClick: () -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        placeholder = { Text(text = stringResource(R.string.message)) },
        value = message,
        colors = OutlinedTextFieldDefaults.colors(),
        leadingIcon = {
            IconButton(onClick = onEmojiClick) {
                Icon(
                    imageVector = Icons.Rounded.InsertEmoticon,
                    contentDescription = null
                )
            }
        },
        trailingIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onAttachmentClick) {
                    Icon(
                        imageVector = Icons.Rounded.Attachment,
                        contentDescription = null
                    )
                }
                IconButton(onClick = onCameraClick) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(onSend = onDraftMessageSend),
        onValueChange = onDraftMessageChanged,
    )
}

@Preview(showBackground = true)
@Composable
private fun DraftMessageFieldPreview() {
    FlexChatTheme {
        DraftMessageField(
            message = "asdfasdfasdf",
            onDraftMessageChanged = {},
            onDraftMessageSend = {},
            onEmojiClick = {},
            onAttachmentClick = {},
            onCameraClick = {},
        )
    }
}
