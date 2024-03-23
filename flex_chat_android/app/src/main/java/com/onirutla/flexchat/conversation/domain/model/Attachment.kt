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

import com.onirutla.flexchat.core.data.model.AttachmentResponse
import kotlinx.datetime.LocalDateTime

data class Attachment(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val messageId: String = "",
    val url: String = "",
    val name: String = "",
    val mimeType: String = "",
    val createdAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
)

internal fun Attachment.toAttachmentResponse() = AttachmentResponse(
    id = id,
    userId = userId,
    conversationId = conversationId,
    messageId = messageId,
    url = url,
    name = name,
    mimeType = mimeType
)
