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
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parMap
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.core.data.FirebaseCollections
import com.onirutla.flexchat.core.data.models.UserResponse
import com.onirutla.flexchat.core.data.models.toUser
import com.onirutla.flexchat.domain.models.RegisterWithUsernameEmailAndPassword
import com.onirutla.flexchat.domain.models.User
import com.onirutla.flexchat.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
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

    override suspend fun getCurrentUser(): User? = _currentUser.toList().firstOrNull()

    override val currentUser: Flow<User> = _currentUser.filterNotNull()
        .flatMapLatest { user ->
            userRef.document(user.id)
                .snapshots()
                .map { it.toObject<UserResponse>() }
                .mapNotNull { it?.toUser() }
        }.onEach { Timber.d("mappedCurrentUser: $it") }

    override val isLoggedIn: Flow<Boolean> = _currentUser.mapLatest { it != null }

    override suspend fun login(
        email: String,
        password: String,
    ): Either<Throwable, AuthResult> = either {
        ensure(email.isNotBlank() or email.isEmpty()) {
            raise(IllegalArgumentException("Email should not be blank or empty"))
        }
        ensure(password.isNotBlank() or password.isEmpty()) {
            raise(IllegalArgumentException("Password should not be blank or empty"))
        }

        val signInResult = Either
            .catch { firebaseAuth.signInWithEmailAndPassword(email, password).await() }
            .onLeft { raise(it) }
            .getOrNull()

        ensureNotNull(signInResult) {
            raise(NullPointerException("SignInResult should not be null"))
        }

        signInResult
    }

    override suspend fun getSignInIntentSender(): Either<Throwable, IntentSender> {
        val googleIdRequestOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true)
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("543116269827-mlp3gfm76q1gjvqrj3k6dg808cb5r221.apps.googleusercontent.com")
            .build()

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(googleIdRequestOptions)
            .build()

        val signInResult = Either.catch {
            signInClient.beginSignIn(signInRequest)
                .await()
                .pendingIntent
                .intentSender
        }.onLeft { Timber.e(it) }

        return signInResult
    }

    override suspend fun loginWithGoogle(intent: Intent): Either<Throwable, User> = either {
        val signInCredential = Either.catch { signInClient.getSignInCredentialFromIntent(intent) }
            .onLeft { raise(it) }
            .getOrNull()

        ensureNotNull(signInCredential) { raise(NullPointerException("SignInCredential should not be null")) }
        val googleIdToken = signInCredential.googleIdToken
        val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

        val firebaseUser = Either.catch {
            firebaseAuth.signInWithCredential(googleCredential)
                .await()
                .user
        }.onLeft { raise(it) }
            .getOrNull()

        ensureNotNull(firebaseUser) { raise(NullPointerException("FirebaseUser should not be null")) }
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

        saveUserToFireStore(userResponse)
            .onLeft { raise(it) }
        userResponse.toUser()
    }

    override suspend fun registerWithEmailAndPassword(
        registerArg: RegisterWithUsernameEmailAndPassword,
    ): Either<Throwable, Unit> = either {
        with(registerArg) {
            ensure(username.isNotBlank() or username.isNotEmpty()) {
                raise(IllegalArgumentException("Username should not be empty"))
            }
            ensure(email.isNotBlank() or email.isNotEmpty()) {
                raise(IllegalArgumentException("Email should not be empty"))
            }
            ensure(password.isNotBlank() or password.isNotEmpty()) {
                raise(IllegalArgumentException("Password should not be empty"))
            }
        }
        val result = with(registerArg) {
            Either.catch { firebaseAuth.createUserWithEmailAndPassword(email, password).await() }
        }.onLeft { raise(it) }
            .getOrNull()
        ensureNotNull(result) { raise(NullPointerException("AuthResult should not be null")) }

        val user = result.user
        ensureNotNull(user) { raise(NullPointerException("FirebaseUser should not be null")) }
        saveUserToFireStore(
            with(user) {
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
        ).onLeft { raise(it) }
    }

    override fun getUserByUsername(username: String): Flow<List<User>> = userRef
        .orderBy("username")
        .startAt(username)
        .endAt("$username\uf8ff")
        .snapshots()
        .map { snapshot -> snapshot.parMap { it.toObject<UserResponse>().toUser() } }
        .catch { Timber.e(it) }

    private suspend fun saveUserToFireStore(
        userResponse: UserResponse,
    ): Either<Throwable, Unit> = Either.catch {
        userRef.document(userResponse.id)
            .set(userResponse)
            .await()
    }
}
