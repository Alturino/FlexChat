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

package com.onirutla.flexchat.di

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.onirutla.flexchat.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(): FirebaseApp = FirebaseApp.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth =
        if (BuildConfig.DEBUG) {
            FirebaseAuth.getInstance(firebaseApp).apply {
                useEmulator("10.0.2.2", 9099)
            }
        } else {
            FirebaseAuth.getInstance(firebaseApp)
        }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(firebaseApp: FirebaseApp): FirebaseDatabase =
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance(firebaseApp).apply {
                useEmulator("10.0.2.2", 9000)
            }
        } else {
            FirebaseDatabase.getInstance(firebaseApp)
        }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp): FirebaseFirestore =
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance(firebaseApp).apply {
                useEmulator("10.0.2.2", 8080)
            }
        } else {
            FirebaseFirestore.getInstance(firebaseApp)
        }
}
