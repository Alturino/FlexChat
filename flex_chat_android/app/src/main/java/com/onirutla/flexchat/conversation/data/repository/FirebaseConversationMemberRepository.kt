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
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.ConversationMemberResponse
import com.onirutla.flexchat.conversation.data.model.toConversationMember
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import com.onirutla.flexchat.user.data.model.UserResponse
import com.onirutla.flexchat.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationMemberRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
) : ConversationMemberRepository {

    private val conversationMemberRef = firestore
        .collection(FirebaseCollections.CONVERSATION_MEMBERS)

    private val userRef = firestore.collection(FirebaseCollections.USERS)

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

    override suspend fun conversationMemberById(
        id: String,
    ): Either<Throwable, ConversationMember> = Either.catch {
        val conversationMemberResponse = conversationMemberRef.document(id)
            .get()
            .await()
            .toObject<ConversationMemberResponse>()!!
        val messages = conversationMemberResponse.messageIds
            .map { messageId ->
                messageRepository.messageById(messageId).getOrElse { throw it }
            }
        conversationMemberResponse.toConversationMember(messages = messages)
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
        conversationId: String,
        userId: String,
    ): Either<Throwable, ConversationMember> = either {
        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
            raise(IllegalArgumentException("User id should not be empty or null"))
        }
        ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
            raise(IllegalArgumentException("Conversation id should not be empty or null"))
        }

        val conversationMembers = conversationMemberRef.whereEqualTo("userId", userId)
            .whereEqualTo("conversationId", conversationId)
            .get()
            .await()
            .toObjects<ConversationMemberResponse>()

        val isConversationMembersExist = conversationMembers.isNotEmpty()
        if (isConversationMembersExist) {
            val conversationMember = conversationMembers
                .first { it.conversationId == conversationId && it.userId == userId }
            conversationMember.toConversationMember()
        } else {
            Either.catch {
                firestore.runTransaction {
                    val user = it.get(userRef.document(userId)).toObject<UserResponse>()!!

                    val conversationMemberId = conversationMemberRef.document().id
                    val conversationMemberResponse = ConversationMemberResponse(
                        id = conversationMemberId,
                        userId = userId,
                        conversationId = conversationId,
                        email = user.email,
                        photoProfileUrl = user.photoProfileUrl,
                        username = user.username,
                        messageIds = listOf()
                    )

                    it.set(
                        conversationMemberRef.document(conversationMemberId),
                        conversationMemberResponse
                    )
                    conversationMemberResponse.toConversationMember()
                }.await()
            }.bind()
        }
    }

}
