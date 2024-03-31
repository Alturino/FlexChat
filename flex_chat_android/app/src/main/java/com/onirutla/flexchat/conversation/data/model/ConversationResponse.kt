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

package com.onirutla.flexchat.conversation.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal data class ConversationResponse(
    val id: String = "",
    val conversationName: String = "",
    val isGroup: Boolean = false,
    val slug: String = "",
    val imageUrl: String = "",
    val conversationMemberIds: List<String> = listOf(),
    val userIds: List<String> = listOf(),
    val attachmentIds: List<String> = listOf(),
    val messageIds: List<String> = listOf(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)

internal fun ConversationResponse.toConversation(
    messages: List<Message> = listOf(),
    conversationMembers: List<ConversationMember> = listOf(),
) = Conversation(
    id = id,
    conversationName = conversationName,
    slug = slug,
    isGroup = isGroup,
    imageUrl = imageUrl,
    conversationMembers = conversationMembers,
    conversationMemberIds = conversationMemberIds,
    messages = messages,
    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    deletedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
)

internal inline fun List<ConversationResponse>.toConversations(
    messages: List<Message> = listOf(),
    conversationMembers: List<ConversationMember> = listOf(),
) = map { it.toConversation(messages = messages, conversationMembers = conversationMembers) }
