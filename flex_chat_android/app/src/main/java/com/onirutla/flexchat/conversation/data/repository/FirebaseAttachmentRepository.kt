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

package com.onirutla.flexchat.conversation.data.repository

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.onirutla.flexchat.conversation.data.model.AttachmentArgs
import com.onirutla.flexchat.conversation.domain.repository.AttachmentRepository
import com.onirutla.flexchat.core.data.model.Attachment
import com.onirutla.flexchat.core.domain.model.error_state.CreateAttachmentError
import com.onirutla.flexchat.core.domain.model.error_state.GetAttachmentError
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseAttachmentRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : AttachmentRepository {

    private val attachmentRef = firebaseFirestore.collection(FirebaseCollections.ATTACHMENTS)
    private val attachmentStorage = firebaseStorage.reference

    override suspend fun createAttachment(
        attachmentArgs: AttachmentArgs,
        onProgress: (percent: Double, bytesTransferred: Long, totalByteCount: Long) -> Unit,
    ): Either<CreateAttachmentError, Unit> = either {
        with(attachmentArgs) {
            ensure(userId.isNotEmpty() or userId.isNotBlank()) {
                CreateAttachmentError.UserIdEmptyOrBlank
            }
            ensure(messageId.isNotEmpty() or messageId.isNotBlank()) {
                CreateAttachmentError.MessageIdEmptyOrBlank
            }
            ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
                CreateAttachmentError.ConversationIdEmptyOrBlank
            }
            ensure(uri != Uri.EMPTY) {
                CreateAttachmentError.UriEmpty
            }
        }

        val file = attachmentArgs.uri.toFile()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        val metadata = storageMetadata {
            with(attachmentArgs) {
                contentType = mimeType
                setCustomMetadata("userId", userId)
                setCustomMetadata("conversationId", conversationId)
                setCustomMetadata("messageId", messageId)
            }
        }
        val childRef = attachmentStorage.child("attachments/${file.name}")
        Either.catch {
            childRef.putFile(attachmentArgs.uri, metadata)
                .addOnProgressListener {
                    onProgress(
                        it.bytesTransferred.toDouble() / it.totalByteCount.toDouble(),
                        it.bytesTransferred,
                        it.totalByteCount
                    )
                }
                .await()
        }.onLeft { raise(CreateAttachmentError.FailedToInsertToStorage(it)) }

        val downloadUrl = Either.catch {
            childRef.downloadUrl
                .await()
                .toString()
        }.getOrElse { raise(CreateAttachmentError.FailedToRetrieveDownloadUrl(it)) }

        ensure(downloadUrl.isNotEmpty() or downloadUrl.isNotBlank()) {
            CreateAttachmentError.DownloadUrlEmptyOrBlank
        }

        val attachmentId = attachmentRef.document().id
        val attachment = with(attachmentArgs) {
            Attachment(
                id = attachmentId,
                userId = userId,
                conversationId = conversationId,
                conversationMemberId = conversationMemberId,
                messageId = messageId,
                mimeType = mimeType.orEmpty(),
                senderName = senderName,
                url = downloadUrl,
            )
        }
        attachmentRef.document(attachmentId)
            .set(attachment)
            .addOnFailureListener { raise(CreateAttachmentError.FailedToInsertToDatabase(it)) }
    }

    private suspend fun getAttachmentByField(
        field: String,
        value: String,
    ): Either<GetAttachmentError, List<Attachment>> = Either.catch {
        attachmentRef.whereEqualTo(field, value)
            .get()
            .await()
            .toObjects<Attachment>()
    }.mapLeft { GetAttachmentError.UnknownError(it) }

    override suspend fun getAttachmentByMessageId(
        messageId: String,
    ): Either<GetAttachmentError, List<Attachment>> = either {
        ensure(messageId.isNotBlank() or messageId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "messageId")
        }

        val result = getAttachmentByField("messageId", messageId)
            .bind()

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }

        result
    }

    override suspend fun getAttachmentByConversationId(
        conversationId: String,
    ): Either<GetAttachmentError, List<Attachment>> = either {
        ensure(conversationId.isNotBlank() or conversationId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "conversationId")
        }

        val result = getAttachmentByField("conversationId", conversationId)
            .bind()

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }
        result
    }

    override fun attachmentByMessageIdFlow(
        messageId: String,
    ): Flow<List<Attachment>> = attachmentRef
        .whereEqualTo("messageId", messageId)
        .snapshots()
        .map { it.toObjects<Attachment>() }

    override suspend fun getAttachmentByUserId(
        userId: String,
    ): Either<GetAttachmentError, List<Attachment>> = either {
        ensure(userId.isNotBlank() or userId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "userId")
        }

        val result = getAttachmentByField("userId", userId)
            .bind()

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }
        result
    }

}
