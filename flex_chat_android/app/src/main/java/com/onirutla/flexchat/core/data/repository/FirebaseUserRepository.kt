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

import android.content.Intent
import android.content.IntentSender
import arrow.core.Either
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
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
    private val signInClient: SignInClient,
) : UserRepository {

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private val userRef = firebaseFirestore.collection(FirebaseCollections.USERS)

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
                    )
                }
            } else {
                null
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override val currentUser: Flow<User> = _currentUser.filterNotNull()
        .flatMapLatest { user ->
            userRef.document(user.id)
                .snapshots()
                .map { it.toObject<UserResponse>() }
                .mapNotNull { it?.toUser() }
        }.onEach { Timber.d("mappedCurrentUser: $it") }

    override val isLoggedIn: Flow<Boolean> = _currentUser.mapLatest { it != null }

    override suspend fun login(email: String, password: String): Either<Exception, Unit> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        if (result.user == null) {
            Either.Left(NullPointerException("User cannot be null"))
        } else {
            Either.Right(Unit)
        }
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException) {
            throw e
        }
        Either.Left(e)
    }

    override suspend fun getSignInIntentSender(): Either<Exception, IntentSender> {
        val googleIdRequestOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true)
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("543116269827-mlp3gfm76q1gjvqrj3k6dg808cb5r221.apps.googleusercontent.com")
            .build()

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(googleIdRequestOptions)
            .build()

        val signInResult = try {
            val signInResult = signInClient.beginSignIn(signInRequest).await()
            Either.Right(signInResult.pendingIntent.intentSender)
        } catch (e: Exception) {
            Timber.e(e)
            if (e is CancellationException)
                throw e
            Either.Left(e)
        }

        return signInResult
    }

    override suspend fun loginWithGoogle(intent: Intent): Either<Exception, User> = try {
        val signInCredential = signInClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = signInCredential.googleIdToken
        val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

        val firebaseUser = firebaseAuth.signInWithCredential(googleCredential).await().user
        if (firebaseUser != null) {
            val userResponse = with(firebaseUser) {
                UserResponse(
                    id = uid,
                    username = displayName.orEmpty(),
                    email = email.orEmpty(),
                    password = "",
                    phoneNumber = phoneNumber.orEmpty(),
                    photoProfileUrl = photoUrl.toString(),
                    status = "",
                    isOnline = false,
                )
            }
            saveUserToFireStore(userResponse).onLeft { throw it }
            Either.Right(userResponse.toUser())
        } else {
            Either.Left(NullPointerException("Sign in user is null"))
        }
    } catch (e: Exception) {
        Timber.e(e)
        if (e is CancellationException)
            throw e
        Either.Left(e)
    }

    override suspend fun registerWithEmailAndPassword(
        registerArg: RegisterWithUsernameEmailAndPassword,
    ): Either<Exception, Unit> = try {
        val result = with(registerArg) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        }
        if (result.user == null) {
            Either.Left(NullPointerException("User cannot be null"))
        } else {
            saveUserToFireStore(
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
            )
            Either.Right(Unit)
        }
    } catch (e: Exception) {
        Timber.e(e)
        firebaseAuth.currentUser?.delete()?.await()
        if (e is CancellationException) {
            throw e
        }
        Either.Left(e)
    }

    override fun getUserByUsername(username: String): Flow<List<User>> = userRef
        .orderBy("username")
        .startAt(username)
        .endAt()
        .snapshots()
        .onEach { Timber.d("$it") }
        .map { snapshot -> snapshot.map { it.toObject<UserResponse>().toUser() } }
        .onEach { Timber.d("users: $it") }

    private suspend fun saveUserToFireStore(
        userResponse: UserResponse,
    ): Either<Exception, Unit> = try {
        userRef.document(userResponse.id)
            .set(userResponse)
            .await()
        Either.Right(Unit)
    } catch (e: Exception) {
        Either.Left(e)
    }
}
