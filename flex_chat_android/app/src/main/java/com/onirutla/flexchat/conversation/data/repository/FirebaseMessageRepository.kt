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
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.AttachmentArgs
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.conversation.data.model.Message
import com.onirutla.flexchat.conversation.domain.repository.AttachmentRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val attachmentRepository: AttachmentRepository,
) : MessageRepository {

    private val messageRef = firestore.collection(FirebaseCollections.MESSAGES)
    private val conversationRef = firestore.collection(FirebaseCollections.CONVERSATIONS)

    override suspend fun messageByUserId(
        userId: String,
    ): Either<Throwable, List<Message>> = Either.catch {
        messageRef.whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects<Message>()
    }.onLeft { Timber.e(it) }

    override suspend fun messageById(id: String): Either<Throwable, Message> = Either.catch {
        messageRef.document(id)
            .get()
            .await()
            .toObject<Message>()!!

    }

    override suspend fun messageByConversationId(
        conversationId: String,
    ): Either<Throwable, List<Message>> = Either.catch {
        messageRef.whereEqualTo("conversationId", conversationId)
            .get()
            .await()
            .toObjects<Message>()
    }.onLeft { Timber.e(it) }

    override fun messageByConversationIdFlow(
        conversationId: String,
    ): Flow<List<Message>> = messageRef.whereEqualTo("conversationId", conversationId)
        .snapshots()
        .map { snapshot ->
            snapshot.toObjects<Message>().parMap { it }
        }
        .onEach { Timber.d("messageByConversationIdFlow: $it") }
        .catch { Timber.e(it) }

    override suspend fun messageByConversationMemberId(
        conversationMemberId: String,
    ): Either<Throwable, List<Message>> = Either.catch {
        messageRef.whereEqualTo("conversationMemberId", conversationMemberId)
            .get()
            .await()
            .toObjects<Message>()

    }

    override suspend fun sendMessage(messageRequest: Message): Either<Throwable, Message> = either {
        with(messageRequest) {
            ensure(conversationMemberId.isNotEmpty() or conversationMemberId.isNotBlank()) {
                IllegalArgumentException("conversationMemberId should not be empty or blank")
            }
            ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
                IllegalArgumentException("conversationId should not be empty or blank")
            }
            ensure(userId.isNotEmpty() or userId.isNotBlank()) {
                IllegalArgumentException("userId should not be empty or blank")
            }
            ensure(messageBody.isNotEmpty() or messageBody.isNotBlank()) {
                IllegalArgumentException("messageBody should not be empty or blank")
            }
            ensure(senderName.isNotEmpty() or senderName.isNotBlank()) {
                IllegalArgumentException("senderName should not be empty or blank")
            }
        }


        val id = if (messageRequest.id.isNotEmpty() or messageRequest.id.isNotBlank()) {
            messageRequest.id
        } else {
            messageRef.document().id
        }

        val message = messageRequest.copy(id = id)
        Either.catch {
            firestore.runTransaction {

                val conversation = it.get(conversationRef.document(message.conversationId))
                    .toObject<Conversation>()!!

                it.set(messageRef.document(id), message)
                it.set(
                    conversationRef.document(conversation.id),
                    conversation.copy(messageIds = conversation.messageIds + listOf(message.id))
                )
            }.await()
        }.onLeft { Timber.e(it) }
            .bind()
        message
    }

    override suspend fun sendMessageWithAttachment(
        message: Message,
        uri: Uri,
    ): Either<Throwable, Unit> = either {
        val messageId = sendMessage(messageRequest = message)
            .onLeft { Timber.e(it) }
            .bind()
            .id
        attachmentRepository.createAttachment(
            AttachmentArgs(
                uri = uri,
                messageId = messageId,
                userId = message.userId,
                conversationId = message.conversationId,
                conversationMemberId = message.conversationMemberId,
                senderName = message.senderName
            ),
            onProgress = { _, _, _ -> },
        ).onLeft { Timber.e(it.errorMessage) }
            .onLeft { raise(Throwable(it.errorMessage)) }
    }

    override fun messageByUserIdFlow(
        userId: String,
    ): Flow<List<Message>> = messageRef.whereEqualTo("userId", userId)
        .snapshots()
        .map { snapshot -> snapshot.toObjects<Message>().parMap { it } }
        .onEach { Timber.d("messageByUserIdFlow: $it") }

    override val observeMessage: Flow<List<Message>>
        get() = messageRef.snapshots().map {
            it.toObjects<Message>()

        }

}
