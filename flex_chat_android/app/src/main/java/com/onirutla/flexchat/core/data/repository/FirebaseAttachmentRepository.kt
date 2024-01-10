package com.onirutla.flexchat.core.data.repository

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.AttachmentResponse
import com.onirutla.flexchat.domain.models.error_state.CreateAttachmentError
import com.onirutla.flexchat.domain.models.error_state.GetAttachmentError
import com.onirutla.flexchat.domain.repository.AttachmentRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseAttachmentRepository(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : AttachmentRepository {

    private val attachmentRef = firebaseFirestore.collection(FirebaseCollections.ATTACHMENTS)
    private val attachmentStorage = firebaseStorage.reference

    override suspend fun createAttachment(
        attachment: AttachmentResponse,
        onProgress: (percent: Double, bytesTransferred: Long, totalByteCount: Long) -> Unit,
        uri: Uri,
    ): Either<CreateAttachmentError, Unit> = either {
        with(attachment) {
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

        val file = uri.toFile()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        val metadata = storageMetadata {
            contentType = mimeType
            setCustomMetadata("userId", attachment.userId)
            setCustomMetadata("conversationId", attachment.conversationId)
            setCustomMetadata("messageId", attachment.messageId)
        }
        val childRef = attachmentStorage.child("attachments/${file.name}")
        val uploadTask = childRef.putFile(uri, metadata)
            .addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                Timber.d("Upload file with uri:$uri, with progress: $progress%")
                onProgress(progress, it.bytesTransferred, it.totalByteCount)
            }
            .addOnFailureListener {
                raise(CreateAttachmentError.FailedToInsertToStorage(it))
            }
            .await()

        val downloadUrl = childRef.downloadUrl
            .addOnFailureListener {
                raise(CreateAttachmentError.FailedToRetrieveDownloadUrl(it))
            }
            .await()
            .toString()

        ensure(downloadUrl.isNotEmpty() or downloadUrl.isNotEmpty()) {
            CreateAttachmentError.DownloadUrlEmptyOrBlank
        }

        val attachmentId = attachmentRef.document().id
        val newAttachment = attachment.copy(
            id = attachmentId,
            mimeType = mimeType.orEmpty(),
            url = downloadUrl
        )
        attachmentRef.document(attachmentId)
            .set(newAttachment)
            .addOnFailureListener { raise(CreateAttachmentError.FailedToInsertToDatabase(it)) }
    }

    private suspend fun getAttachmentByField(
        field: String,
        value: String,
    ): Either<GetAttachmentError, List<AttachmentResponse>> = Either.catch {
        attachmentRef.whereEqualTo(field, value)
            .get()
            .await()
            .toObjects<AttachmentResponse>()
    }.mapLeft { GetAttachmentError.UnknownError(it) }

    override suspend fun getAttachmentByMessageId(
        messageId: String,
    ): Either<GetAttachmentError, List<AttachmentResponse>> = either {
        ensure(messageId.isNotBlank() or messageId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "messageId")
        }

        val result = getAttachmentByField("messageId", messageId)
            .onLeft { raise(it) }
            .getOrElse { listOf() }

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }

        result
    }

    override suspend fun getAttachmentByConversationId(
        conversationId: String,
    ): Either<GetAttachmentError, List<AttachmentResponse>> = either {
        ensure(conversationId.isNotBlank() or conversationId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "conversationId")
        }

        val result = getAttachmentByField("conversationId", conversationId)
            .onLeft { raise(it) }
            .getOrElse { listOf() }

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }
        result
    }

    override suspend fun getAttachmentByUserId(
        userId: String,
    ): Either<GetAttachmentError, List<AttachmentResponse>> = either {
        ensure(userId.isNotBlank() or userId.isNotEmpty()) {
            GetAttachmentError.ArgumentError(field = "userId")
        }

        val result = getAttachmentByField("userId", userId)
            .onLeft { raise(it) }
            .getOrElse { listOf() }

        ensure(result.isNotEmpty()) {
            raise(GetAttachmentError.Empty)
        }
        result
    }

}
