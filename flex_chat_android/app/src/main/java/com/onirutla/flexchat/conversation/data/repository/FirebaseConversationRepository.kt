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
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.ConversationResponse
import com.onirutla.flexchat.conversation.data.model.toConversation
import com.onirutla.flexchat.conversation.data.model.toConversations
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val messageRepository: MessageRepository,
) : ConversationRepository {

    private val conversationRef = firestore.collection(FirebaseCollections.CONVERSATIONS)

    //    override suspend fun getConversationByUserId(
//        userId: String,
//    ): Either<Throwable, List<Conversation>> = either {
//        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
//            raise(IllegalArgumentException("User id should not be empty or null"))
//        }
//
//        val userAsConversationMembers = conversationMemberRepository
//            .getConversationMemberByUserId(userId)
//            .onLeft { raise(it) }
//            .getOrNull()
//        ensure(!userAsConversationMembers.isNullOrEmpty()) {
//            raise(NullPointerException("User as conversation members should not be empty or null"))
//        }
//
//        val conversations = userAsConversationMembers.flatMap { userAsConversationMember ->
//            conversationRef.whereEqualTo("id", userAsConversationMember.conversationId)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .get()
//                .await()
//                .toObjects<ConversationResponse>()
//                .map { conversationResponse ->
//                    val conversationMembers = conversationMemberRepository
//                        .getConversationMemberByConversationId(conversationResponse.id)
//                        .onLeft { raise(it) }
//                        .getOrNull()
//                    ensure(!conversationMembers.isNullOrEmpty()) {
//                        NullPointerException("Conversation members should not be null")
//                    }
//
//                    conversationResponse.toConversation(
//                        conversationMembers = conversationMembers,
//                        messages = conversationMembers.flatMap { it.messages }
//                            .sortedByDescending { it.createdAt },
//                    )
//                }
//        }.sortedByDescending { it.latestMessage.createdAt }
//        conversations
//    }
//
//
//    override suspend fun observeConversationByUserId(userId: String): Flow<List<Conversation>> {
//        val conversationByUserId = conversationMemberRepository
//            .observeConversationMemberByUserId(userId)
//            .mapLatest { conversationMembers ->
//                conversationMembers.parMapNotNull { conversationMember ->
//                    val conversation = getConversationById(conversationMember.conversationId)
//                        .onLeft { Timber.e(it) }
//                        .onRight { Timber.d("conversation: $it") }
//                        .getOrNull()
//                    conversation?.copy(
//                        conversationName = conversation.conversationName.split(" ")
//                            .filterNot { it == conversationMember.username }
//                            .joinToString("")
//                    )
//                }
//            }
//
//        return combine(
//            conversationByUserId,
//            messageRepository.observeMessage
//        ) { conversations, messages ->
//            conversations.parMap { conversation ->
//                val filteredMessages = messages.filter { it.conversationId == conversation.id }
//                conversation.copy(
//                    messages = filteredMessages,
//                    latestMessage = filteredMessages.maxByOrNull { it.createdAt } ?: Message()
//                )
//            }
//        }
//    }
//
//
//    override suspend fun getConversationById(
//        conversationId: String,
//    ): Either<Throwable, Conversation> = either {
//        val conversationMembers = conversationMemberRepository
//            .getConversationMemberByConversationId(conversationId)
//            .bind()
//
//        ensure(conversationMembers.isNotEmpty()) {
//            NullPointerException("conversation members should not be null or empty")
//        }
//
//        val conversation = Either.catch {
//            conversationRef
//                .document(conversationId)
//                .get()
//                .await()
//                .toObject<ConversationResponse>()
//                ?.toConversation(
//                    conversationMembers = conversationMembers,
//                    messages = conversationMembers.flatMap { it.messages }
//                        .sortedByDescending { it.createdAt }
//                )
//        }.bind()
//        ensureNotNull(conversation) {
//            NullPointerException("Conversation should not be null")
//        }
//
//        conversation
//    }
//
    override suspend fun getConversationByUserId(
        userId: String,
    ): Either<Throwable, List<Conversation>> = Either.catch {
        conversationRef.whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects<ConversationResponse>()
            .toConversations(
                conversationMembers = {
                    conversationMemberRepository.conversationMemberByUserId(userId)
                        .getOrElse { throw it }
                },
                messages = {
                    messageRepository.messageByUserId(userId)
                        .getOrElse { throw it }
                }
            )
    }

    override suspend fun getConversationById(conversationId: String): Either<Throwable, Conversation> =
        Either.catch {
            conversationRef
                .document(conversationId)
                .get()
                .await()
                .toObject<ConversationResponse>()!!
                .toConversation(
                    messages = {
                        messageRepository.messageByConversationId(conversationId)
                            .getOrElse { throw it }
                    },
                    conversationMembers = {
                        conversationMemberRepository.conversationMemberByConversationId(
                            conversationId
                        )
                            .getOrElse { throw it }
                    }
                )
        }

    override suspend fun createConversation(
        conversation: Conversation,
    ): Either<Throwable, String> = either {
        with(conversation) {
            ensure(conversationMembers.isNotEmpty() && conversationMembers.size >= 2) {
                raise(IllegalArgumentException("conversationMembers should not be empty and at least 2"))
            }
            ensure(slug.isNotEmpty() or slug.isNotBlank()) {
                raise(IllegalArgumentException("slug should not be empty or blank"))
            }
        }

        val conversations = Either.catch {
            conversationRef.whereEqualTo("slug", conversation.slug)
                .get()
                .await()
                .toObjects<ConversationResponse>()
        }.bind()

        val isConversationExist = conversations.isNotEmpty()
        if (isConversationExist) {
            conversations.first().id
        } else {
            val newConversationId = conversationRef.document().id
            val newConversation = conversation.copy(id = newConversationId)
            Either.catch {
                conversationRef.document(newConversationId)
                    .set(newConversation)
                    .await()
            }.bind()
            newConversationId
        }
    }

    override suspend fun conversationByUserIdFlow(userId: String): Flow<List<Conversation>> {
//
//        val conversationFlow = conversationRef.whereEqualTo("userId", userId)
//            .snapshots()
//            .map { it.toObjects<ConversationResponse>() }
//            .catch { Timber.e(it) }
//
//        val messageFlow = messageRepository.messageByUserIdFlow(userId)
//
//        val conversationMemberFlow = conversationMemberRepository.conversationMemberByUserIdFlow(userId)
        return flow {}
    }

}
