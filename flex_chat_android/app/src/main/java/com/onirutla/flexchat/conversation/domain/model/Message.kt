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

import com.onirutla.flexchat.conversation.data.model.MessageResponse
import java.time.LocalDateTime

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val conversationMemberId: String = "",
    val userId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val messageBody: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val deletedAt: LocalDateTime = LocalDateTime.now(),
)

internal fun Message.toMessageResponse() = MessageResponse(
    id = id,
    conversationId = conversationId,
    conversationMemberId = conversationMemberId,
    userId = userId,
    senderName = senderName,
    senderPhotoUrl = senderPhotoUrl,
    messageBody = messageBody,
)

internal fun List<Message>.toMessageResponses() = map { it.toMessageResponse() }
