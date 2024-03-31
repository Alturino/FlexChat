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

import com.google.firebase.Timestamp
import com.onirutla.flexchat.conversation.data.model.ConversationMemberResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date

data class ConversationMember(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val username: String = "",
    val photoProfileUrl: String = "",
    val email: String = "",
    val messageIds: List<String> = listOf(),
    val messages: List<Message> = listOf(),
    val joinedAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val leftAt: LocalDateTime? = null,
)

internal fun ConversationMember.toConversationMemberResponse() = ConversationMemberResponse(
    id = id,
    userId = userId,
    conversationId = conversationId,
    username = username,
    photoProfileUrl = photoProfileUrl,
    email = email,
    messageIds = messageIds,
    joinedAt = Timestamp(
        Date.from(joinedAt.toInstant(TimeZone.currentSystemDefault()).toJavaInstant())
    ),
    updatedAt = Timestamp(
        Date.from(updatedAt.toInstant(TimeZone.currentSystemDefault()).toJavaInstant())
    ),
    leftAt = Timestamp(
        Date.from(leftAt?.toInstant(TimeZone.currentSystemDefault())?.toJavaInstant())
    ),
)

internal fun List<ConversationMember>.toConversationMemberResponses() =
    map { it.toConversationMemberResponse() }
