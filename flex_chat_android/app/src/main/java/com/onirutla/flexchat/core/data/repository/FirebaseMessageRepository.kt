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

import android.net.Uri
import arrow.core.Either
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.MessageResponse
import com.onirutla.flexchat.core.data.models.toMessage
import com.onirutla.flexchat.domain.models.Message
import com.onirutla.flexchat.domain.repository.MessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : MessageRepository {

    private val messageRef = firebaseFirestore.collection(FirebaseCollections.MESSAGES)

    override suspend fun getMessageByUserId(
        userId: String,
    ): Either<Exception, List<Message>> = try {
        val messages = messageRef.whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects<MessageResponse>()
            .parMap { it.toMessage() }
        Either.Right(messages)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override suspend fun getMessageByConversationId(
        conversationId: String,
    ): Either<Exception, List<Message>> = try {
        val messages = messageRef.whereEqualTo("conversationId", conversationId)
            .get()
            .await()
            .toObjects<MessageResponse>()
            .parMap { it.toMessage() }
        Either.Right(messages)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override val observeMessage: Flow<List<Message>>
        get() = messageRef.snapshots().mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().parMap { it.toMessage() }
        }

    override fun observeMessageByConversationId(
        conversationId: String,
    ): Flow<List<Message>> = messageRef.whereEqualTo("conversationId", conversationId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().parMap { it.toMessage() }
        }

    override fun observeMessageByUserId(userId: String): Flow<List<Message>> = messageRef
        .whereEqualTo("userId", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .mapLatest { snapshot ->
            snapshot.toObjects<MessageResponse>().parMap { it.toMessage() }
        }.catch { Timber.e("observeMessageByUserId on line 107: $it") }

    override suspend fun getMessageByConversationMemberId(
        conversationMemberId: String,
    ): Either<Exception, List<Message>> = try {
        val messages = messageRef
            .whereEqualTo("conversationMemberId", conversationMemberId)
            .get()
            .await()
            .toObjects<MessageResponse>()
            .parMap { it.toMessage() }
        Either.Right(messages)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override suspend fun createMessage(
        messageResponse: MessageResponse,
    ): Either<Exception, String> = try {
        val messageId = messageRef.document().id
        messageRef.document(messageId).set(messageResponse.copy(id = messageId))
            .await()
        Either.Right(messageId)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override fun createMessageWithAttachment(
        messageResponse: MessageResponse,
        uri: Uri,
    ): Flow<Unit> = callbackFlow {
        createMessage(messageResponse)
            .onRight { }
            .onLeft { }

        firebaseFirestore.
    }
}
