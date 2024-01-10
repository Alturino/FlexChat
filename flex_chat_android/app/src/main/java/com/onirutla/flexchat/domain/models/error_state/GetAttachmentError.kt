package com.onirutla.flexchat.domain.models.error_state

import com.google.android.play.integrity.internal.j

sealed class GetAttachmentError(val message: String) {
    data object Empty : GetAttachmentError("Attachments is empty")
    data class ArgumentError(val field: String): GetAttachmentError("$field should not be empty or blank")
    data class UnknownError(val throwable: Throwable) : GetAttachmentError(throwable.localizedMessage.orEmpty())
}
