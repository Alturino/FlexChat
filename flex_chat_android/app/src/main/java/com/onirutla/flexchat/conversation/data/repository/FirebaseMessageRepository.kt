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
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.MessageResponse
import com.onirutla.flexchat.conversation.data.model.toMessage
import com.onirutla.flexchat.conversation.domain.model.Message
import com.onirutla.flexchat.conversation.domain.repository.AttachmentRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseMessageRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val attachmentRepository: AttachmentRepository,
) : MessageRepository {

    private val messageRef = firebaseFirestore.collection(FirebaseCollections.MESSAGES)

    override suspend fun getMessageByUserId(
        userId: String,
    ): Either<Throwable, List<Message>> = either {
        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
            raise(IllegalArgumentException("User id should not be empty or blank"))
        }
        val messages = Either.catch {
            messageRef.whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects<MessageResponse>()
                .parMap { it.toMessage() }
        }.bind()
        messages
    }

    override suspend fun getMessageByConversationId(
        conversationId: String,
    ): Either<Throwable, List<Message>> = either {
        ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
            raise(IllegalArgumentException("Conversation id should not be empty or blank"))
        }
        val messages = Either.catch {
            messageRef.whereEqualTo("conversationId", conversationId)
                .get()
                .await()
                .toObjects<MessageResponse>()
                .parMap { it.toMessage() }
        }.bind()
        messages
    }

    override val observeMessage: Flow<List<Message>> = messageRef
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().map { it.toMessage() }
        }

    override fun observeMessageByConversationId(
        conversationId: String,
    ): Flow<List<Message>> = messageRef.whereEqualTo("conversationId", conversationId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().map { it.toMessage() }
        }.catch {
            Timber.e(it)
        }

    override fun observeMessageByUserId(userId: String): Flow<List<Message>> = messageRef
        .whereEqualTo("userId", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().map { it.toMessage() }
        }.catch { Timber.e(it) }

    override suspend fun getMessageByConversationMemberId(
        conversationMemberId: String,
    ): Either<Throwable, List<Message>> = either {
        ensure(conversationMemberId.isNotEmpty() or conversationMemberId.isNotBlank()) {
            IllegalArgumentException("Conversation member id should not be empty or blank")
        }
        val messages = Either.catch {
            messageRef
                .whereEqualTo("conversationMemberId", conversationMemberId)
                .get()
                .await()
                .toObjects<MessageResponse>()
                .map { it.toMessage() }
        }.bind()
        messages
    }

    override suspend fun createMessage(
        message: Message,
    ): Either<Throwable, String> = either {
        with(message) {
            ensure(userId.isNotEmpty() or userId.isNotBlank()) {
                IllegalArgumentException("User id should not be empty or blank")
            }
            ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
                IllegalArgumentException("Conversation id should not be empty or blank")
            }
            ensure(conversationMemberId.isNotEmpty() or conversationMemberId.isNotBlank()) {
                IllegalArgumentException("Conversation member id should not be empty or blank")
            }
            ensure(messageBody.isNotEmpty() or messageBody.isNotBlank()) {
                IllegalArgumentException("Message body should not be empty or blank")
            }
            ensure(senderName.isNotEmpty() or senderName.isNotBlank()) {
                IllegalArgumentException("Sender name should not be empty or blank")
            }
        }
        val messageId = messageRef.document().id
        Either.catch {
            messageRef.document(messageId)
                .set(message.copy(id = messageId))
                .await()
        }.bind()
        messageId
    }

    // TODO: Not yet implemented
    override fun createMessageWithAttachment(
        message: Message,
        uri: Uri,
    ): Flow<Unit> = callbackFlow {
        createMessage(message)
            .onRight { }
            .onLeft { }

    }
}
