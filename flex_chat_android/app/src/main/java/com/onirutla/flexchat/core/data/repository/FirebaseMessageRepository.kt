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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.MessageResponse
import com.onirutla.flexchat.core.data.models.toMessage
import com.onirutla.flexchat.domain.models.Message
import com.onirutla.flexchat.domain.repository.MessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : MessageRepository {

    private val messageCollectionRef = firebaseFirestore.collection(FirebaseCollections.MESSAGES)

    override fun getMessageByUserId(userId: String): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageByConversationId(
        conversationId: String,
    ): Either<Exception, List<Message>> = try {
        val messages = messageCollectionRef.whereEqualTo("conversationId", conversationId)
            .get()
            .await()
            .toObjects<MessageResponse>()
            .map { it.toMessage() }
        Either.Right(messages)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override fun observeMessageByConversationId(
        conversationId: String,
    ): Flow<List<Message>> = messageCollectionRef.whereEqualTo("conversationId", conversationId)
        .snapshots()
        .mapNotNull { snapshot ->
            snapshot.toObjects<MessageResponse>().map { it.toMessage() }
        }

    override suspend fun getMessageByConversationMemberId(
        conversationMemberId: String,
    ): Either<Exception, List<Message>> = try {
        val messages = messageCollectionRef
            .whereEqualTo("conversationMemberId", conversationMemberId)
            .get()
            .await()
            .toObjects<MessageResponse>()
            .map { it.toMessage() }
        Either.Right(messages)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }
}
