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
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.ConversationResponse
import com.onirutla.flexchat.core.data.models.toConversation
import com.onirutla.flexchat.domain.models.Conversation
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConversationRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val conversationMemberRepository: ConversationMemberRepository,
) : ConversationRepository {

    private val conversationRef = firebaseFirestore.collection(FirebaseCollections.CONVERSATIONS)

    override suspend fun getConversationByUserId(
        userId: String,
    ): Either<Exception, List<Conversation>> = try {
        val userAsConversationMembers = conversationMemberRepository
            .getConversationMemberByUserId(userId)
            .onLeft { Timber.e(it) }
            .onRight { Timber.d("conversationMembers Right: $it") }
            .fold(ifLeft = { listOf() }, ifRight = { it })

        val conversations = userAsConversationMembers.flatMap { userAsConversationMember ->
            conversationRef.whereEqualTo("id", userAsConversationMember.conversationId)
                .get()
                .await()
                .toObjects<ConversationResponse>()
                .map { conversationResponse ->
                    val conversationMembers = conversationMemberRepository
                        .getConversationMemberByConversationId(conversationResponse.id)
                        .onLeft { Timber.e(it) }
                        .onRight { Timber.d("$it") }
                        .fold(ifLeft = { listOf() }, ifRight = { it })

                    conversationResponse.toConversation(
                        conversationMembers = conversationMembers,
                        messages = conversationMembers.flatMap { it.messages }
                            .sortedByDescending { it.createdAt },
                    )
                }
        }.sortedByDescending { it.latestMessage.createdAt }
        Either.Right(conversations)
    } catch (e: Exception) {
        Timber.e("on line 87:30 $e")
        if (e is CancellationException) throw e
        Either.Left(e)
    }

    override fun observeConversationByUserIdAndConversationId(
        userId: String,
        conversationId: String,
    ): Flow<List<Conversation>> = conversationMemberRepository
        .observeConversationMemberByUserId(userId)
        .flatMapLatest {
            conversationRef.whereEqualTo("id", conversationId)
                .snapshots()
                .map { it.toObjects<ConversationResponse>() }
                .filterNot { it.isEmpty() }
                .map { conversationResponses ->
                    conversationResponses.parMap { conversationResponse ->
                        val conversationMembers = conversationMemberRepository
                            .getConversationMemberByConversationId(conversationResponse.id)
                            .onLeft { Timber.e("on line 106:61 $it") }
                            .onRight { Timber.d("$it") }
                            .fold(ifLeft = { listOf() }, ifRight = { it })

                        conversationResponse.toConversation(
                            conversationMembers,
                            conversationMembers.flatMap { it.messages }
                        )
                    }
                }
        }

    override suspend fun getConversationById(conversationId: String): Conversation {
        val conversationMembers = conversationMemberRepository
            .getConversationMemberByConversationId(conversationId)
            .onLeft { Timber.e("on line 121:33 $it") }
            .onRight { Timber.d("$it") }
            .fold(ifLeft = { listOf() }, ifRight = { it })

        return conversationRef
            .document(conversationId)
            .get()
            .await()
            .toObject<ConversationResponse>()
            ?.toConversation(
                conversationMembers = conversationMembers,
                messages = conversationMembers.flatMap { it.messages }
                    .sortedByDescending { it.createdAt }
            ) ?: Conversation()
    }

    override suspend fun createConversation(
        conversationResponse: ConversationResponse,
    ): Either<Exception, String> = try {
        val conversations = conversationRef.whereEqualTo("slug", conversationResponse.slug)
            .get()
            .await()
            .toObjects<ConversationResponse>()

        val isConversationExist = conversations.isNotEmpty()
        if (isConversationExist) {
            Either.Right(conversations.first().id)
        } else {
            val newConversationId = conversationRef.document().id
            val newConversation = conversationResponse.copy(id = newConversationId)
            conversationRef.document(newConversationId)
                .set(newConversation)
                .await()
            Either.Right(newConversationId)
        }
    } catch (e: Exception) {
        Timber.e("on line 157:33 $e")
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

}
