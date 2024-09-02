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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.core.util.FirebaseCollections
import com.onirutla.flexchat.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
) : ConversationRepository {

    private val conversationRef = firestore.collection(FirebaseCollections.CONVERSATIONS)
    private val userRef = firestore.collection(FirebaseCollections.USERS)

    override fun conversationsByUserIdFlow(userId: String): Flow<List<Conversation>> =
        conversationRef.whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects<Conversation>() }
            .onEach { Timber.d("conversationByUserIdFlow: $it") }.catch { Timber.e(it) }

    override fun conversationByIdFlow(conversationId: String): Flow<Conversation> =
        conversationRef.document(conversationId)
            .snapshots()
            .mapNotNull { it.toObject<Conversation>() }
            .onEach { Timber.d("conversationByIdFlow: $it") }.catch { Timber.e(it) }

    override suspend fun conversationById(
        conversationId: String,
    ): Either<Throwable, Conversation> = Either.catch {
        conversationRef.document(conversationId)
            .get()
            .await()
            .toObject<Conversation>()!!
    }

    override suspend fun createConversation(
        userIds: List<String>,
    ): Either<Throwable, Conversation> = either {
        ensure(userIds.isNotEmpty()) {
            IllegalArgumentException("userIds shouldn't be empty")
        }

        val isConversationNotExist = Either.catch {
            conversationRef.whereIn("userIds", userIds)
                .get()
                .await()
                .isEmpty
        }.bind()
        ensure(isConversationNotExist) {
            IllegalStateException("Conversation with userIds: $userIds is already exist")
        }

        firestore.runTransaction {

        }
//        val users = userIds.mapNotNull { userRepository.getUserById(it).getOrNull() }
//            .onEach {userRepository.upsertUser()}
//        val conversationId = conversationRef.document().id
//        val conversation = Conversation(
//            id = conversationId,
//            conversationName = users.joinToString { it.username },
//            userIds = userIds,
//            isGroup = userIds.size > 2,
//            slug = userIds.joinToString { it }
//        )
//        Either.catch { conversationRef.document(conversationId).set(conversation).await() }
//            .onLeft { Timber.e(it) }
//            .bind()
//        conversation
        Conversation()
    }

}
