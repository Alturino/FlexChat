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

package com.onirutla.flexchat.auth.data.repository

import android.content.Intent
import android.content.IntentSender
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.recover
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.onirutla.flexchat.BuildConfig.SIGN_IN_CLIENT_SERVER_CLIENT_ID
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.auth.login.domain.data.LoginRequest
import com.onirutla.flexchat.auth.register.domain.data.RegisterRequest
import com.onirutla.flexchat.core.util.firebaseUserFlow
import com.onirutla.flexchat.user.domain.model.User
import com.onirutla.flexchat.user.domain.repository.UserRepository
import com.onirutla.flexchat.user.util.toUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseAuthRepository @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val signInClient: SignInClient,
) : AuthRepository {

    private val _currentUser: Flow<FirebaseUser?> = firebaseAuth.firebaseUserFlow
        .onEach { Timber.d("FirebaseUser: $it") }

    override val currentUser: Flow<User> = _currentUser.filterNotNull()
        .mapNotNull { firebaseUser ->
            userRepository.getUserById(firebaseUser.uid)
                .getOrElse { throw it }
        }
        .catch { Timber.e(it) }
        .onEach { Timber.d("User from firestore: $it") }

    override val isLoggedIn: Flow<Boolean> = _currentUser.map { it != null }
        .onEach { Timber.d("isLoggedIn: $it") }

    override fun signOut(): Either<Throwable, Unit> = Either.catch {
        firebaseAuth.signOut()
    }

    override suspend fun login(loginRequest: LoginRequest): Either<Throwable, User> = either {
        with(loginRequest) {
            ensure(email.isNotBlank() or email.isNotEmpty()) {
                raise(IllegalArgumentException("Email should not be blank or empty"))
            }
            ensure(password.isNotBlank() or password.isNotEmpty()) {
                raise(IllegalArgumentException("Password should not be blank or empty"))
            }

            val firebaseUser = Either.catch {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .await()
                    .user
            }.onLeft {
                Timber.e(it)
            }.recover<Throwable, Throwable, FirebaseUser?> {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .await()
                    .user
            }.onLeft { Timber.e(it) }
                .bind()

            ensureNotNull(firebaseUser) {
                raise(NullPointerException("FirebaseUser should not be null"))
            }

            userRepository.upsertUser(firebaseUser.toUser())
                .onLeft { firebaseUser.delete().await() }
                .bind()
        }
    }

    override suspend fun getSignInIntentSender(): Either<Throwable, IntentSender> {
        val googleIdRequestOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true)
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(SIGN_IN_CLIENT_SERVER_CLIENT_ID)
            .build()

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(googleIdRequestOptions)
            .build()

        return Either.catch {
            signInClient.beginSignIn(signInRequest)
                .await()
                .pendingIntent
                .intentSender
        }.onLeft { Timber.e(it) }
    }

    override suspend fun loginWithGoogle(intent: Intent): Either<Throwable, User> = either {
        val signInCredential = Either.catch { signInClient.getSignInCredentialFromIntent(intent) }
            .bind()

        ensureNotNull(signInCredential) {
            raise(NullPointerException("SignInCredential should not be null"))
        }

        val googleIdToken = signInCredential.googleIdToken
        val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

        val firebaseUser = Either.catch {
            firebaseAuth.signInWithCredential(googleCredential)
                .await()
                .user
        }.bind()

        ensureNotNull(firebaseUser) {
            raise(NullPointerException("FirebaseUser should not be null"))
        }

        userRepository.upsertUser(firebaseUser.toUser())
            .bind()
    }

    override suspend fun registerWithEmailAndPassword(
        registerRequest: RegisterRequest,
    ): Either<Throwable, User> = either {
        with(registerRequest) {
            ensure(username.isNotBlank() or email.isNotEmpty()) {
                raise(IllegalArgumentException("Username should not be blank or empty"))
            }
            ensure(email.isNotBlank() or email.isNotEmpty()) {
                raise(IllegalArgumentException("Email should not be blank or empty"))
            }
            ensure(password.isNotBlank() or password.isNotEmpty()) {
                raise(IllegalArgumentException("Password should not be blank or empty"))
            }

            val firebaseUser = Either.catch {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .await()
                    .user
            }.bind()

            ensureNotNull(firebaseUser) {
                raise(NullPointerException("FirebaseUser should not be null"))
            }

            userRepository.upsertUser(firebaseUser.toUser())
                .bind()
        }
    }


}
