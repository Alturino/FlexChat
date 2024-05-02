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

package com.onirutla.flexchat.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ConversationItem(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = conversation.conversationName) },
        leadingContent = {
            SubcomposeAsyncImage(
                modifier = Modifier.size(50.dp),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                model = conversation.imageUrl,
                contentDescription = null,
                loading = { CircularProgressIndicator() }
            )
        },
        supportingContent = {
//            Text(text = conversation.latestMessage.messageBody)
        },
        trailingContent = {
//            Text(text = conversation.latestMessage.createdAt.format())
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ConversationItemPreview() {
    FlexChatTheme {
        ConversationItem(
            conversation = Conversation(
                id = "voluptatibus",
                conversationName = "Duncan Bruce",
                imageUrl = "https://www.google.com/#q=errem",
                isGroup = false,
//                latestMessage = Message(),
                createdAt = Timestamp.now(),
                deletedAt = Timestamp.now(),
            ),
            onClick = {}
        )
    }
}
