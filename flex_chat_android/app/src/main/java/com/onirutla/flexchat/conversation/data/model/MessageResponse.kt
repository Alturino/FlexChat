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
import com.onirutla.flexchat.conversation.domain.model.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

@IgnoreExtraProperties
internal data class MessageResponse(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val conversationMemberId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val messageBody: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)

internal fun MessageResponse.toMessage() = Message(
    id = id,
    conversationId = conversationId,
    conversationMemberId = conversationMemberId,
    userId = userId,
    senderName = senderName,
    senderPhotoUrl = senderPhotoUrl,
    messageBody = messageBody,
    createdAt = createdAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC) ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
    updatedAt = updatedAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC) ?: Clock.System.now().toLocalDateTime(TimeZone.UTC),
    deletedAt = deletedAt?.toDate()
        ?.toInstant()
        ?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC)
)

internal fun List<MessageResponse>.toMessages() = map { it.toMessage() }
