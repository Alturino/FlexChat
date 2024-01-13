/*
 * MIT License
 *
 * Copyright (c) 2023 - 2023 Ricky Alturino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.onirutla.flexchat.core.data.repository

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.ConversationMemberResponse
import com.onirutla.flexchat.core.data.models.toConversationMember
import com.onirutla.flexchat.domain.models.ConversationMember
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirebaseConversationMemberRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val messageRepository: MessageRepository,
) : ConversationMemberRepository {

    private val conversationMemberRef = firebaseFirestore
        .collection(FirebaseCollections.CONVERSATION_MEMBERS)

    override suspend fun getConversationMemberByUserId(
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
                .parMap { conversationMemberResponse ->
                    val messages = messageRepository
                        .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                        .onLeft { raise(it) }
                        .getOrNull()
                    ensure(!messages.isNullOrEmpty()) {
                        raise(NullPointerException("Message should not be empty or"))
                    }
                    conversationMemberResponse.toConversationMember(messages = messages)
                }
        }.bind()

        ensure(conversationMembers.isNotEmpty()) {
            raise(NullPointerException("Conversation members should not be empty or null"))
        }

        conversationMembers
    }

    override fun observeConversationMemberByUserId(
        userId: String,
    ): Flow<List<ConversationMember>> = conversationMemberRef.whereEqualTo("userId", userId)
        .orderBy("joinedAt", Query.Direction.DESCENDING)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<ConversationMemberResponse>()
                .parMap { conversationMemberResponse ->
                    val messages = messageRepository
                        .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                        .onRight { Timber.d("$it") }
                        .fold(ifLeft = { listOf() }, ifRight = { it })
                    conversationMemberResponse.toConversationMember(messages = messages)
                }
        }

    override fun observeConversationMemberByConversationId(conversationId: String) =
        conversationMemberRef.whereEqualTo("conversationId", conversationId)
            .snapshots()
            .mapLatest { snapshot ->
                snapshot.toObjects<ConversationMemberResponse>()
                    .parMap { conversationMemberResponse ->
                        val messages = messageRepository
                            .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                            .onRight { Timber.d("$it") }
                            .fold(ifLeft = { listOf() }, ifRight = { it })
                        conversationMemberResponse.toConversationMember(messages = messages)
                    }
            }

    override suspend fun getConversationMemberByConversationId(
        conversationId: String,
    ): Either<Throwable, List<ConversationMember>> = either {
        ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
            raise(IllegalArgumentException("Conversation id should not be empty or blank"))
        }

        val conversationMembers = Either.catch {
            conversationMemberRef
                .whereEqualTo("conversationId", conversationId)
                .orderBy("joinedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects<ConversationMemberResponse>()
                .parMap { conversationMemberResponse ->
                    val messages = messageRepository
                        .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                        .bind()
                    conversationMemberResponse.toConversationMember(messages = messages)
                }
        }.bind()
        ensure(conversationMembers.isNotEmpty()) {
            raise(NullPointerException("Conversation members should not be empty"))
        }
        conversationMembers
    }

    override suspend fun createConversationMember(
        conversationMemberResponse: ConversationMemberResponse,
    ): Either<Throwable, String> = either {
        with(conversationMemberResponse) {
            ensure(userId.isNotEmpty() or userId.isNotBlank()) {
                raise(IllegalArgumentException("User id should not be empty or null"))
            }
            ensure(conversationId.isNotEmpty() or conversationId.isNotBlank()) {
                raise(IllegalArgumentException("Conversation id should not be empty or null"))
            }
        }

        val conversationMembers = getConversationMembersByUserIdAndConversationId(
            userId = conversationMemberResponse.userId,
            conversationId = conversationMemberResponse.conversationId
        ).bind()

        val isConversationMemberExist = conversationMembers.isNotEmpty()
        if (isConversationMemberExist) {
            val conversationMemberId = conversationMembers.firstOrNull()?.id
            ensureNotNull(conversationMemberId) {
                raise(NullPointerException("Conversation member id should not be null"))
            }
            conversationMemberId
        } else {
            val newConversationMemberId = conversationMemberRef.document().id
            val newConversationMember = conversationMemberResponse
                .copy(id = newConversationMemberId)
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
