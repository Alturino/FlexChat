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

package com.onirutla.flexchat.core.domain.model.error_state

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
