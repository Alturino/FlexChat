/*
 * MIT License
 *
 * Copyright (c) 2023 Ricky Alturino
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

package com.onirutla.flexchat.di

import com.onirutla.flexchat.core.data.repository.FirebaseConversationMemberRepository
import com.onirutla.flexchat.core.data.repository.FirebaseConversationRepository
import com.onirutla.flexchat.core.data.repository.FirebaseMessageRepository
import com.onirutla.flexchat.core.data.repository.FirebaseUserRepository
import com.onirutla.flexchat.domain.repository.ConversationMemberRepository
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.MessageRepository
import com.onirutla.flexchat.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepository: FirebaseUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(firebaseConversationRepository: FirebaseConversationRepository): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindConversationMemberRepository(firebaseConversationMemberRepository: FirebaseConversationMemberRepository): ConversationMemberRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(messageRepository: FirebaseMessageRepository): MessageRepository
}
