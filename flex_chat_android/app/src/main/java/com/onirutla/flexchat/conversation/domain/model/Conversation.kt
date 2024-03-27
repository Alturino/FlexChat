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

package com.onirutla.flexchat.conversation.domain.model

import com.onirutla.flexchat.conversation.data.model.ConversationResponse
import kotlinx.datetime.LocalDateTime

data class Conversation(
    val id: String = "",
    val conversationName: String = "",
    val slug: String = "",
    val isGroup: Boolean = false,
    val imageUrl: String = "",
    val conversationMembers: List<ConversationMember> = listOf(),
    val conversationMemberIds: List<String> = listOf(),
    val attachments: List<Attachment> = listOf(),
    val attachmentIds: List<String> = listOf(),
    val messages: List<Message> = conversationMembers.flatMap { it.messages }
        .sortedByDescending { it.createdAt },
//    val latestMessage: Message = messages.maxByOrNull { it.createdAt } ?: Message(),
    val messageIds: List<String> = listOf(),
    val createdAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
)

internal fun Conversation.toConversationResponse() = ConversationResponse(
    id = id,
    conversationName = conversationName,
    isGroup = isGroup,
    imageUrl = imageUrl,
    slug = slug,
)
