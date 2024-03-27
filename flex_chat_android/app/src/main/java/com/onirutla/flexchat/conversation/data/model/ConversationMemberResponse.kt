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
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

internal data class ConversationMemberResponse(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val username: String = "",
    val email: String = "",
    val photoProfileUrl: String = "",
    val messageIds: List<String> = listOf(),
    @ServerTimestamp
    val joinedAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val leftAt: Timestamp? = null,
)

internal fun ConversationMemberResponse.toConversationMember(
    messages: List<Message>,
) = ConversationMember(
    id = id,
    userId = userId,
    conversationId = conversationId,
    email = email,
    username = username,
    photoProfileUrl = photoProfileUrl,
    messages = messages,
    joinedAt = joinedAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC) ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
    updatedAt = updatedAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC) ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
    leftAt = leftAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC) ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
)

internal fun List<ConversationMemberResponse>.toConversationMembers(
    messages: List<Message>,
): List<ConversationMember> = map { it.toConversationMember(messages = messages) }
