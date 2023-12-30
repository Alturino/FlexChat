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
import java.util.concurrent.CancellationException
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
    ): Either<Exception, List<ConversationMember>> = try {
        val conversationMembers = conversationMemberRef.whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects<ConversationMemberResponse>()
            .parMap { conversationMemberResponse ->
                val messages = messageRepository
                    .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                    .onLeft { Timber.e(it) }
                    .onRight { Timber.d("$it") }
                    .fold(ifLeft = { listOf() }, ifRight = { it })
                conversationMemberResponse.toConversationMember(messages = messages)
            }
        Either.Right(conversationMembers)
    } catch (e: Exception) {
        Timber.e("on line 74:30 $e")
        if (e is CancellationException) throw e
        Either.Left(e)
    }

    override fun observeConversationMemberByUserId(
        userId: String,
    ): Flow<List<ConversationMember>> = conversationMemberRef.whereEqualTo("userId", userId)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<ConversationMemberResponse>()
                .parMap { conversationMemberResponse ->
                    val messages = messageRepository
                        .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                        .onLeft { Timber.e(it) }
                        .onRight { Timber.d("$it") }
                        .fold(ifLeft = { listOf() }, ifRight = { it })
                    conversationMemberResponse.toConversationMember(messages = messages)
                }
                .sortedByDescending { it.joinedAt }
        }

    override suspend fun getConversationMemberByConversationId(
        conversationId: String,
    ): Either<Exception, List<ConversationMember>> = try {
        val conversationMembers = conversationMemberRef
            .whereEqualTo("conversationId", conversationId)
            .get()
            .await()
            .toObjects<ConversationMemberResponse>()
            .parMap { conversationMemberResponse ->
                val messages = messageRepository
                    .getMessageByConversationMemberId(conversationMemberId = conversationMemberResponse.id)
                    .onLeft { Timber.e("$it") }
                    .onRight { Timber.d("$it") }
                    .fold(ifLeft = { listOf() }, ifRight = { it })
                conversationMemberResponse.toConversationMember(messages = messages)
            }
        Either.Right(conversationMembers)
    } catch (e: Exception) {
        Timber.e("on line 114:30 $e")
        if (e is CancellationException) throw e
        Either.Left(e)
    }

    override suspend fun createConversationMember(
        conversationMemberResponse: ConversationMemberResponse,
    ): Either<Exception, String> = try {
        val conversationMembers = conversationMemberRef
            .whereEqualTo("userId", conversationMemberResponse.userId)
            .whereEqualTo("conversationId", conversationMemberResponse.conversationId)
            .get()
            .await()
            .toObjects<ConversationMemberResponse>()

        val isConversationMemberExist = conversationMembers.isNotEmpty()
        if (isConversationMemberExist) {
            Either.Right(conversationMembers.first().id)
        } else {
            val newConversationMemberId = conversationMemberRef.document().id
            val newConversationMember = conversationMemberResponse
                .copy(id = newConversationMemberId)
            conversationMemberRef.document(newConversationMemberId)
                .set(newConversationMember)
                .await()
            Either.Right(newConversationMemberId)
        }
    } catch (e: Exception) {
        Timber.e("on line 142:30 $e")
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }
}
