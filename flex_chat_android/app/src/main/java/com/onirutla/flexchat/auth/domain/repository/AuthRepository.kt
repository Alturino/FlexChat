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

package com.onirutla.flexchat.auth.domain.repository

import android.content.Intent
import android.content.IntentSender
import arrow.core.Either
import com.onirutla.flexchat.auth.login.domain.data.LoginRequest
import com.onirutla.flexchat.auth.register.domain.data.RegisterRequest
import com.onirutla.flexchat.user.data.model.User
import kotlinx.coroutines.flow.Flow

internal interface AuthRepository {
    fun signOut(): Either<Throwable, Unit>
    suspend fun getSignInIntentSender(): Either<Throwable, IntentSender>
    suspend fun loginWithGoogle(intent: Intent): Either<Throwable, User>
    suspend fun getCurrentUser(): User
    val currentUserFlow: Flow<User>
    val isLoggedIn: Flow<Boolean>
    suspend fun loginWithEmailAndPassword(request: LoginRequest): Either<Throwable, User>
    suspend fun registerWithEmailAndPassword(request: RegisterRequest): Either<Throwable, User>
}
