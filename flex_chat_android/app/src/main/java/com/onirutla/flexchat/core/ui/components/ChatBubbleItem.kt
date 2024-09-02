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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.onirutla.flexchat.conversation.data.model.Message
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.core.ui.util.format
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ChatBubbleItem(
    modifier: Modifier = Modifier,
    message: Message,
    isMySelf: Boolean,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ElevatedCard(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(fraction = 0.8f)
                .align(if (isMySelf) Alignment.End else Alignment.Start),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (isMySelf) 24.dp else 0.dp,
                bottomEnd = if (isMySelf) 0.dp else 24.dp
            ),
            elevation = CardDefaults.elevatedCardElevation(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = message.messageBody,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                    softWrap = true,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = message.createdAt
                        ?.toDate()
                        ?.toInstant()
                        ?.toKotlinInstant()
                        ?.toLocalDateTime(TimeZone.currentSystemDefault())
                        ?.format()
                        .orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ChatBubblePreview() {
    FlexChatTheme {
        ChatBubbleItem(
            message = Message(
                id = "ornare",
                conversationId = "suas",
                userId = "viris",
                senderName = "Mauricio Barron",
                senderPhotoUrl = "https://www.google.com/#q=elitr",
                messageBody = "lacusfadsfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdffasdfadf",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                deletedAt = null
            ),
            isMySelf = true
        )
    }
}
