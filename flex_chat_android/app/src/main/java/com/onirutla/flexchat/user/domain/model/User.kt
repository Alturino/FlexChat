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

package com.onirutla.flexchat.user.domain.model

import com.google.firebase.Timestamp
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.user.data.model.UserResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val photoProfileUrl: String = "",
    val phoneNumber: String = "",
    val status: String = "",
    val isOnline: Boolean? = null,
    val conversationIds: List<String> = listOf(),
    val conversations: List<Conversation> = listOf(),
    val conversationMemberIds: List<String> = listOf(),
    val conversationMembers: List<ConversationMember> = listOf(),
    val createdAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val deletedAt: LocalDateTime? = null,
)

internal fun User.toUserResponse(): UserResponse = UserResponse(
    id = id,
    username = username,
    email = email,
    password = password,
    phoneNumber = phoneNumber,
    photoProfileUrl = photoProfileUrl,
    status = status,
    isOnline = isOnline ?: false,
    createdAt = Timestamp(
        Date.from(
            createdAt.toInstant(TimeZone.currentSystemDefault()).toJavaInstant()
        )
    ),
    updatedAt = Timestamp(
        Date.from(
            updatedAt.toInstant(TimeZone.currentSystemDefault()).toJavaInstant()
        )
    ),
    deletedAt = if (deletedAt == null) null else Timestamp(
        Date.from(deletedAt.toInstant(TimeZone.currentSystemDefault()).toJavaInstant())
    ),
)
