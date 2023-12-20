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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.UserResponse
import com.onirutla.flexchat.core.data.models.toUser
import com.onirutla.flexchat.domain.models.User
import com.onirutla.flexchat.domain.repository.RegisterWithUsernameEmailAndPassword
import com.onirutla.flexchat.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirebaseUserRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : UserRepository {

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private val _currentUser: Flow<User?> = callbackFlow {
        val listener = AuthStateListener {
            val user = if (it.currentUser != null) {
                with(it.currentUser) {
                    User(
                        id = this?.uid.orEmpty(),
                        username = this?.displayName.orEmpty(),
                        email = this?.email.orEmpty(),
                        photoProfileUrl = this?.photoUrl.toString(),
                        phoneNumber = this?.phoneNumber.orEmpty(),
                        status = "",
                        conversation = listOf(),
                        createdAt = "",
                        deletedAt = "",
                    )
                }
            } else {
                null
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            Timber.d("removing auth state listener: $listener")
            firebaseAuth.removeAuthStateListener(listener)
            Timber.d("removing auth state listener: $listener")
        }
    }

    override val currentUser: Flow<User> = _currentUser.filterNotNull()
        .onEach { Timber.d("currentUser: $it") }
        .mapLatest {
            val userFromFirestore = firebaseFirestore.collection(FirebaseCollections.USERS)
                .document(it.id)
                .get()
                .await()
                .toObject<UserResponse>()
                ?.toUser()

            Timber.d("userFromFirestore: $userFromFirestore")

            it.copy(
                username = userFromFirestore?.username.orEmpty(),
                status = userFromFirestore?.status.orEmpty(),
                createdAt = userFromFirestore?.createdAt.orEmpty(),
                deletedAt = userFromFirestore?.deletedAt.orEmpty()
            )
        }.onEach { Timber.d("mappedCurrentUser: $it") }

    override val isLoggedIn: Flow<Boolean> = _currentUser.mapLatest { it != null }

    override suspend fun login(email: String, password: String): Either<Exception, Unit> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        if (result.user == null) {
            Either.Left(NullPointerException("User cannot be null"))
        }
        firebaseFirestore.collection(FirebaseCollections.USERS)
            .add(
                with(result.user!!) {
                    UserResponse(
                        id = uid,
                        username = displayName.orEmpty(),
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber.orEmpty(),
                        photoProfileUrl = photoUrl.toString(),
                        status = "",
                        isOnline = false,
                    )
                }
            ).await()
        Either.Right(Unit)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException) {
            Either.Left(e)
            throw e
        }
        Either.Left(e)
    }

    override suspend fun loginWithGoogle() {

    }

    override suspend fun registerWithEmailAndPassword(
        registerArg: RegisterWithUsernameEmailAndPassword,
    ): Either<Exception, Unit> = try {
        val result = with(registerArg) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }
        if (result.user == null) {
            Either.Left(NullPointerException("User cannot be null"))
        }
        firebaseFirestore.collection(FirebaseCollections.USERS)
            .document("${result.user?.uid}")
            .set(
                with(result.user!!) {
                    with(registerArg) {
                        UserResponse(
                            id = uid,
                            username = username,
                            email = email,
                            password = password,
                            phoneNumber = phoneNumber.orEmpty(),
                            photoProfileUrl = photoUrl.toString(),
                            status = "",
                            isOnline = false,
                        )
                    }
                }
            ).await()
        Either.Right(Unit)
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException) {
            Either.Left(e)
            throw e
        }
        Either.Left(e)
    }

    override fun getUserByUsername(username: String): Flow<List<User>> = firebaseFirestore
        .collection(FirebaseCollections.USERS)
        .whereEqualTo("username", username)
        .snapshots()
        .map { snapshot -> snapshot.map { it.toObject<UserResponse>().toUser() } }
        .onEach { Timber.d("users: $it") }

}
