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

package com.onirutla.flexchat.conversation.domain.repository

import arrow.core.Either
import com.onirutla.flexchat.conversation.data.model.request.AttachmentRequest
import com.onirutla.flexchat.core.data.model.Attachment
import com.onirutla.flexchat.core.domain.model.error_state.CreateAttachmentError
import com.onirutla.flexchat.core.domain.model.error_state.GetAttachmentError
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    suspend fun getAttachmentByConversationId(conversationId: String): Either<GetAttachmentError, List<Attachment>>
    suspend fun getAttachmentByMessageId(messageId: String): Either<GetAttachmentError, List<Attachment>>
    suspend fun getAttachmentByUserId(userId: String): Either<GetAttachmentError, List<Attachment>>
    suspend fun createAttachment(
        attachmentRequest: AttachmentRequest,
        onProgress: (percent: Double, bytesTransferred: Long, totalByteCount: Long) -> Unit,
    ): Either<CreateAttachmentError, Unit>

    fun attachmentByMessageIdFlow(messageId: String): Flow<List<Attachment>>
}

