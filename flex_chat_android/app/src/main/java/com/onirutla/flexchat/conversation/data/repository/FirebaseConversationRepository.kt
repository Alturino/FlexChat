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
import com.onirutla.flexchat.conversation.data.model.ConversationResponse
import com.onirutla.flexchat.conversation.data.model.toConversation
import com.onirutla.flexchat.conversation.data.model.toConversations
import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.domain.repository.MessageRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val conversationMemberRepository: ConversationMemberRepository,
    private val messageRepository: MessageRepository,
) : ConversationRepository {

    private val conversationRef = firestore.collection(FirebaseCollections.CONVERSATIONS)
    private val conversationMemberRef =
        firestore.collection(FirebaseCollections.CONVERSATION_MEMBERS)

    override suspend fun conversationByUserId(
        userId: String,
    ): Either<Throwable, List<Conversation>> = coroutineScope {
        Either.catch {
            val messages = async {
                messageRepository.messageByUserId(userId)
                    .getOrElse { throw it }
            }

            val conversationMembers = async {
                conversationMemberRepository
                    .conversationMemberByUserId(userId)
                    .getOrElse { throw it }
            }

            conversationRef.whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects<ConversationResponse>()
                .toConversations(
                    conversationMembers = conversationMembers.await(),
                    messages = messages.await()
                )
        }
    }

    override suspend fun getConversationById(
        conversationId: String,
    ): Either<Throwable, Conversation> = coroutineScope {
        Either.catch {
            val messages = async {
                messageRepository.messageByConversationId(conversationId)
                    .getOrElse { throw it }
            }

            val conversationMembers = async {
                conversationMemberRepository
                    .conversationMemberByConversationId(conversationId)
                    .getOrElse { throw it }
            }

            conversationRef
                .document(conversationId)
                .get()
                .await()
                .toObject<ConversationResponse>()!!
                .toConversation(
                    messages = messages.await(),
                    conversationMembers = conversationMembers.await(),
                )
        }
    }

    override suspend fun createConversation(
        userIds: List<String>,
    ): Either<Throwable, Conversation> = either {
        userIds.forEach {
            ensure(it.isNotEmpty() or it.isNotBlank()) {
                IllegalArgumentException("userId shouldn't be empty or blank")
            }
        }

        val slug = userIds.joinToString()
        val conversations = Either.catch {
            conversationRef.whereEqualTo("slug", slug)
                .get()
                .await()
                .toObjects<ConversationResponse>()
                .toConversations()
        }.bind()

        val isConversationExist = conversations.isNotEmpty()
        if (isConversationExist) {
            conversations.first()
        } else {
            val conversationId = conversationRef.document().id
            val conversationMembers = userIds.parMap(Dispatchers.IO) { userId ->
                conversationMemberRepository.createConversationMember(
                    conversationId = conversationId,
                    userId = userId
                ).bind()
            }
            val conversationResponse = ConversationResponse(
                id = conversationId,
                slug = slug,
                userIds = userIds,
                conversationMemberIds = conversationMembers.map { it.id },
                conversationName = conversationMembers.joinToString(" ") { it.username }
            )
            Either.catch {
                conversationRef.document(conversationId)
                    .set(conversationResponse)
                    .await()
            }.bind()
            conversationResponse.toConversation()
        }
    }

    override fun conversationsByUserIdFlow(
        userId: String,
    ): Flow<List<Conversation>> {
        val conversations = conversationRef
            .whereArrayContains("userIds", userId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<ConversationResponse>() }
            .onEach { Timber.d("$it") }
            .catch { Timber.e(it) }

        val conversationMembers =
            conversationMemberRepository.conversationMemberByUserIdFlow(userId)
        val messages = messageRepository.messageByUserIdFlow(userId)
        return combine(
            conversations,
            conversationMembers,
            messages
        ) { conversations, conversationMembers, messages ->
            conversations.toConversations(messages, conversationMembers)
        }
    }

}
