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
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.ConversationResponse
import com.onirutla.flexchat.core.data.models.toConversation
import com.onirutla.flexchat.domain.models.Conversation
import com.onirutla.flexchat.domain.models.Message
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConversationRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val messageRepository: MessageRepository,
) : ConversationRepository {

    private val conversationRef = firebaseFirestore.collection(FirebaseCollections.CONVERSATIONS)

    override suspend fun getConversationByUserId(
        userId: String,
    ): Either<Throwable, List<Conversation>> = either {
        ensure(userId.isNotEmpty() or userId.isNotBlank()) {
            raise(IllegalArgumentException("User id should not be empty or null"))
        }

        val userAsConversationMembers = conversationMemberRepository
            .getConversationMemberByUserId(userId)
            .onLeft { raise(it) }
            .getOrNull()
        ensure(!userAsConversationMembers.isNullOrEmpty()) {
            raise(NullPointerException("User as conversation members should not be empty or null"))
        }

        val conversations = userAsConversationMembers.flatMap { userAsConversationMember ->
            conversationRef.whereEqualTo("id", userAsConversationMember.conversationId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects<ConversationResponse>()
                .map { conversationResponse ->
                    val conversationMembers = conversationMemberRepository
                        .getConversationMemberByConversationId(conversationResponse.id)
                        .onLeft { raise(it) }
                        .getOrNull()
                    ensure(!conversationMembers.isNullOrEmpty()) {
                        NullPointerException("Conversation members should not be null")
                    }

                    conversationResponse.toConversation(
                        conversationMembers = conversationMembers,
                        messages = conversationMembers.flatMap { it.messages }
                            .sortedByDescending { it.createdAt },
                    )
                }
        }.sortedByDescending { it.latestMessage.createdAt }
        conversations
    }


    override suspend fun observeConversationByUserId(userId: String): Flow<List<Conversation>> {
        val conversationByUserId = conversationMemberRepository
            .observeConversationMemberByUserId(userId)
            .mapLatest { conversationMembers ->
                conversationMembers.parMap { conversationMember ->
                    getConversationById(conversationMember.conversationId)
                        .getOrElse { Conversation() }
                }
            }

        return combine(
            conversationByUserId,
            messageRepository.observeMessage
        ) { conversations, messages ->
            conversations.parMap { conversation ->
                val filteredMessages = messages.filter { it.conversationId == conversation.id }
                conversation.copy(
                    messages = filteredMessages,
                    latestMessage = filteredMessages.maxByOrNull { it.createdAt } ?: Message()
                )
            }
        }
    }


    override suspend fun getConversationById(
        conversationId: String,
    ): Either<Throwable, Conversation> = either {
        val conversationMembers = conversationMemberRepository
            .getConversationMemberByConversationId(conversationId)
            .onLeft { raise(it) }
            .getOrNull()

        ensure(!conversationMembers.isNullOrEmpty()) {
            raise(NullPointerException("conversation members should not be null or empty"))
        }

        val result = conversationRef
            .document(conversationId)
            .get()
            .await()
            .toObject<ConversationResponse>()
            ?.toConversation(
                conversationMembers = conversationMembers,
                messages = conversationMembers.flatMap { it.messages }
                    .sortedByDescending { it.createdAt }
            ) ?: Conversation()

        result
    }

    override suspend fun createConversation(
        conversationResponse: ConversationResponse,
    ): Either<Throwable, String> = either {
        with(conversationResponse) {
            ensure(slug.isNotEmpty() or slug.isNotBlank()) {
                raise(IllegalArgumentException("slug should not be empty or blank"))
            }
        }
        val conversations = conversationRef.whereEqualTo("slug", conversationResponse.slug)
            .get()
            .await()
            .toObjects<ConversationResponse>()

        val isConversationExist = conversations.isNotEmpty()
        if (isConversationExist) {
            conversations.first().id
        } else {
            val newConversationId = conversationRef.document().id
            val newConversation = conversationResponse.copy(id = newConversationId)
            conversationRef.document(newConversationId)
                .set(newConversation)
                .await()
            newConversationId
        }
    }

}
