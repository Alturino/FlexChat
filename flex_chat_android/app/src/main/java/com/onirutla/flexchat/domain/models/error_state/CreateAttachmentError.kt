package com.onirutla.flexchat.domain.models.error_state

sealed class CreateAttachmentError(val errorMessage: String) {
    data object UserIdEmptyOrBlank : CreateAttachmentError("User id should not be empty or blank")
    data object MessageIdEmptyOrBlank : CreateAttachmentError("Message id should not be empty or blank")
    data object ConversationIdEmptyOrBlank : CreateAttachmentError("Conversation id should not be empty or blank")
    data object UriEmpty : CreateAttachmentError("Uri should not be empty")
    data class FailedToRetrieveDownloadUrl(val throwable: Throwable) : CreateAttachmentError(throwable.localizedMessage.orEmpty())
    data object DownloadUrlEmptyOrBlank : CreateAttachmentError("Download url should not be empty or blank")
    data class FailedToInsertToStorage(val throwable: Throwable) : CreateAttachmentError(throwable.localizedMessage.orEmpty())
    data class FailedToInsertToDatabase(val throwable: Throwable) : CreateAttachmentError(throwable.localizedMessage.orEmpty())
    data class UnknownError(val throwable: Throwable) : CreateAttachmentError(throwable.localizedMessage.orEmpty())
}
