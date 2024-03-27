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

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.ConversationMemberResponse
import com.onirutla.flexchat.conversation.data.model.toConversationMember
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationMemberRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val messageRepository: MessageRepository,
) : ConversationMemberRepository {

    private val conversationMemberRef = firebaseFirestore
        .collection(FirebaseCollections.CONVERSATION_MEMBERS)

    override suspend fun conversationMemberByUserId(
        userId: String,
    ): Either<Throwable, List<ConversationMember>> = either {
        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
            raise(IllegalArgumentException("userId should not be empty or blank"))
        }

        val conversationMembers = Either.catch {
            conversationMemberRef.whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects<ConversationMemberResponse>()
                .map { conversationMemberResponse ->
                    val messages = messageRepository
                        .messageByConversationMemberId(conversationMemberResponse.id)
                        .bind()
                    conversationMemberResponse.toConversationMember(messages = messages)
                }
        }.bind()

        ensure(conversationMembers.isNotEmpty()) {
            raise(NullPointerException("conversationMembers should not be empty or null"))
        }

        conversationMembers
    }

    override fun conversationMemberByUserIdFlow(userId: String): Flow<List<ConversationMember>> {
        val conversationMemberResponseFlow = conversationMemberRef.whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects<ConversationMemberResponse>() }

        val messageFlow = messageRepository.messageByUserIdFlow(userId)

        val res = combine(
            conversationMemberResponseFlow,
            messageFlow
        ) { conversationMemberResponses: List<ConversationMemberResponse>, messages: List<Message> ->
            conversationMemberResponses.map { conversationMemberResponse ->
                conversationMemberResponse.toConversationMember(
                    messages.filter { message -> message.conversationMemberId == conversationMemberResponse.id })
            }
        }
        return res
    }

    override fun conversationMemberByConversationIdFlow(conversationId: String): Flow<List<ConversationMember>> {
        val conversationMemberResponse = conversationMemberRef
            .whereEqualTo("conversationId", conversationId)
            .snapshots()
            .map { it.toObjects<ConversationMemberResponse>() }

        val message = messageRepository.messageByConversationIdFlow(conversationId)

        return combine(
            conversationMemberResponse,
            message
        ) { conversationMemberResponses: List<ConversationMemberResponse>, messages: List<Message> ->
            conversationMemberResponses.map { response ->
                response.toConversationMember(messages.filter { it.conversationMemberId == response.id })
            }
        }
    }


    override suspend fun conversationMemberByConversationId(
        conversationId: String,
    ): Either<Throwable, List<ConversationMember>> = either {
        ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
            raise(IllegalArgumentException("conversationId should not be empty or blank"))
        }

        val conversationMembers = Either.catch {
            conversationMemberRef.whereEqualTo("conversationId", conversationId)
                .get()
                .await()
                .toObjects<ConversationMemberResponse>()
                .parMap { conversationMemberResponse ->
                    conversationMemberResponse.toConversationMember(
                        messages = messageRepository.messageByConversationMemberId(
                            conversationMemberResponse.id
                        )
                            .onLeft { Timber.e(it) }
                            .bind()
                    )
                }
        }.bind()

        ensure(conversationMembers.isNotEmpty()) {
            raise(NullPointerException("Conversation members should not be empty"))
        }

        conversationMembers
    }

    override suspend fun createConversationMember(
        conversationMember: ConversationMember,
    ): Either<Throwable, String> = either {
        with(conversationMember) {
            ensure(userId.isNotEmpty() or userId.isNotBlank()) {
                raise(IllegalArgumentException("User id should not be empty or null"))
            }
            ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
                raise(IllegalArgumentException("Conversation id should not be empty or null"))
            }
        }

        val conversationMembers = getConversationMembersByUserIdAndConversationId(
            userId = conversationMember.userId,
            conversationId = conversationMember.conversationId
        ).bind()

        val isConversationMemberExist = conversationMembers.isNotEmpty()
        if (isConversationMemberExist) {
            val conversationMemberId = conversationMembers.firstOrNull()?.id
            ensureNotNull(conversationMemberId) {
                raise(NullPointerException("conversationMemberId should not be null"))
            }
            conversationMemberId
        } else {
            val newConversationMemberId = conversationMemberRef.document().id
            val newConversationMember = conversationMember.copy(id = newConversationMemberId)
            Either.catch {
                conversationMemberRef.document(newConversationMemberId)
                    .set(newConversationMember)
                    .await()
            }.bind()
            newConversationMemberId
        }
    }

    private suspend fun getConversationMembersByUserIdAndConversationId(
        userId: String,
        conversationId: String,
    ): Either<Throwable, List<ConversationMemberResponse>> = either {
        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
            IllegalArgumentException("User id should not be null")
        }
        ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
            IllegalArgumentException("Conversation id should not be null")
        }
        Either.catch {
            conversationMemberRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("conversationId", conversationId)
                .get()
                .await()
                .toObjects<ConversationMemberResponse>()
        }.bind()
    }
}
