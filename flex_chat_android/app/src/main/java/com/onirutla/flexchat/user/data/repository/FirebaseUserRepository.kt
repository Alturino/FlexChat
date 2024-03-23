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

package com.onirutla.flexchat.user.data.repository

import arrow.core.Either
import arrow.fx.coroutines.parMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.core.util.FirebaseCollections
import com.onirutla.flexchat.user.data.model.UserResponse
import com.onirutla.flexchat.user.data.model.toUser
import com.onirutla.flexchat.user.domain.model.User
import com.onirutla.flexchat.user.domain.model.toUserResponse
import com.onirutla.flexchat.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    private val userRef = firestore.collection(FirebaseCollections.USERS)

    override suspend fun upsertUser(user: User): Either<Throwable, User> = Either.catch {
        val userResponse = user.toUserResponse()
        userRef.document(user.id)
            .set(userResponse)
            .await()
        user
    }

    override suspend fun getUserById(id: String): Either<Throwable, User> = Either.catch {
        userRef.document(id)
            .get()
            .await()
            .toObject<User>()!!
    }

    override fun getUserByUsername(username: String): Flow<List<User>> = userRef
        .orderBy("username")
        .startAt(username)
        .endAt("$username\uf8ff")
        .snapshots()
        .map { snapshot -> snapshot.parMap { it.toObject<UserResponse>().toUser() } }
        .catch { Timber.e(it) }
}
