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

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@IgnoreExtraProperties
internal data class ConversationMemberResponse(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val username: String = "",
    val photoProfileUrl: String = "",
    @ServerTimestamp
    val joinedAt: Date? = null,
    @ServerTimestamp
    val leftAt: Date? = null,
)

internal fun ConversationMemberResponse.toConversationMember(messages: List<Message>) = ConversationMember(
    id = id,
    userId = userId,
    conversationId = conversationId,
    username = username,
    photoProfileUrl = photoProfileUrl,
    messages = messages,
    joinedAt = LocalDateTime.ofInstant(
        joinedAt?.toInstant() ?: Date().toInstant(),
        ZoneId.systemDefault()
    ),
    leftAt = LocalDateTime.ofInstant(
        leftAt?.toInstant() ?: Date().toInstant(),
        ZoneId.systemDefault()
    ),
)
