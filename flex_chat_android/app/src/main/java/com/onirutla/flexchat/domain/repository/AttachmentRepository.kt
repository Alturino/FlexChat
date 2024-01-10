package com.onirutla.flexchat.domain.repository

import android.net.Uri
import arrow.core.Either
import com.onirutla.flexchat.core.data.models.AttachmentResponse
import com.onirutla.flexchat.domain.models.error_state.CreateAttachmentError
import com.onirutla.flexchat.domain.models.error_state.GetAttachmentError

interface AttachmentRepository {
    suspend fun getAttachmentByConversationId(conversationId: String): Either<GetAttachmentError, List<AttachmentResponse>>
    suspend fun getAttachmentByMessageId(messageId: String): Either<GetAttachmentError, List<AttachmentResponse>>
    suspend fun getAttachmentByUserId(userId: String): Either<GetAttachmentError, List<AttachmentResponse>>
    suspend fun createAttachment(
        attachment: AttachmentResponse,
        onProgress: (percent: Double) -> Unit,
        uri: Uri,
    ): Either<CreateAttachmentError, Unit>
}

