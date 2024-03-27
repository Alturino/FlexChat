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
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@IgnoreExtraProperties
internal data class ConversationResponse(
    val id: String = "",
    val conversationName: String = "",
    val isGroup: Boolean = false,
    val slug: String = "",
    val imageUrl: String = "",
    val conversationMemberIds: List<String> = listOf(),
    val attachmentIds: List<String> = listOf(),
    val messageIds: List<String> = listOf(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)

internal suspend inline fun ConversationResponse.toConversation(
    messages: (ids: List<String>) -> List<Message>,
    conversationMembers: (ids: List<String>) -> List<ConversationMember>,
) = Conversation(
    id = id,
    conversationName = conversationName,
    slug = slug,
    isGroup = isGroup,
    imageUrl = imageUrl,
    conversationMembers = conversationMembers(conversationMemberIds),
    messages = messages(messageIds),
    createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
    deletedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
)

internal suspend inline fun List<ConversationResponse>.toConversations(
    messages: (ids: List<String>) -> List<Message>,
    conversationMembers: (ids: List<String>) -> List<ConversationMember>,
) = map { it.toConversation(messages = messages, conversationMembers = conversationMembers) }