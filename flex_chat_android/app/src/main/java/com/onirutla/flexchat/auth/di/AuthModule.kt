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

package com.onirutla.flexchat.auth.di

import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.onirutla.flexchat.auth.data.repository.FirebaseAuthRepository
import com.onirutla.flexchat.user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AuthModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        userRepository: UserRepository,
        firebaseAuth: FirebaseAuth,
        signInClient: SignInClient,
    ): FirebaseAuthRepository = FirebaseAuthRepository(
        userRepository = userRepository,
        firebaseAuth = firebaseAuth,
        signInClient = signInClient
    )
}
