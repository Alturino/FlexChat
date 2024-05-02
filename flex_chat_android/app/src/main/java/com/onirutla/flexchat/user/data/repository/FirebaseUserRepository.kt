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
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parMap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.onirutla.flexchat.core.util.FirebaseCollections
import com.onirutla.flexchat.user.data.model.User
import com.onirutla.flexchat.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    private val userRef = firestore.collection(FirebaseCollections.USERS)

    override suspend fun upsertUser(user: User): Either<Throwable, User> = Either.catch {
        userRef.document(user.id)
            .set(user.copy(updatedAt = null))
            .await()
        user
    }.onLeft { Timber.e(it) }

    override suspend fun getUserById(id: String): Either<Throwable, User> = either {
        val user = userRef.document(id)
            .get()
            .await()
            .toObject<User>()
        ensureNotNull(user) { Throwable("User is not exist") }
        ensure(user.deletedAt == null) { Throwable("User is not exist") }
        user
    }.onLeft { Timber.e(it) }

    override suspend fun deleteUser(user: User): Either<Throwable, Void> = either {
        val isExist = Either.catch {
            userRef.document(user.id)
                .get()
                .await()
                .exists()
        }.bind()

        ensure(isExist && user.deletedAt == null) {
            IllegalStateException("User is not exist")
        }

        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.currentSystemDefault())
            .toJavaInstant()

        Either.catch {
            userRef.document(user.id)
                .set(user.copy(deletedAt = Timestamp(Date.from(now))))
                .await()
        }.bind()
    }.onLeft { Timber.e(it) }

    override suspend fun userByUsername(
        username: String,
    ): Either<Throwable, List<User>> = Either.catch {
        userRef.orderBy("username")
            .startAt(username)
            .endAt("$username\uf8ff")
            .whereEqualTo("deletedAt", null)
            .get()
            .await()
            .toObjects<User>()
            .parMap { it }
    }.onLeft { Timber.e(it) }

    override fun userByUsernameFlow(username: String): Flow<List<User>> = userRef
        .orderBy("username")
        .startAt(username)
        .endAt("$username\uf8ff")
        .whereEqualTo("deletedAt", null)
        .snapshots()
        .map { snapshot -> snapshot.parMap { it.toObject<User>() } }
        .catch { Timber.e(it) }
}
