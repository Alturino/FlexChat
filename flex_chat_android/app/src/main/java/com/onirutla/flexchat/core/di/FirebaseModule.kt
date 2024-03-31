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

package com.onirutla.flexchat.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.onirutla.flexchat.BuildConfig
import com.onirutla.flexchat.core.util.FirebaseSecret.API_KEY
import com.onirutla.flexchat.core.util.FirebaseSecret.APP_ID
import com.onirutla.flexchat.core.util.FirebaseSecret.EMULATOR_HOST
import com.onirutla.flexchat.core.util.FirebaseSecret.PROJECT_ID
import com.onirutla.flexchat.core.util.FirebaseSecret.STORAGE_BUCKET
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        val firebaseOptions = FirebaseOptions.Builder()
            .setApplicationId(APP_ID)
            .setApiKey(API_KEY)
            .setStorageBucket(STORAGE_BUCKET)
            .setProjectId(PROJECT_ID)
            .build()
        return FirebaseApp.initializeApp(context, firebaseOptions)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return if (BuildConfig.DEBUG) {
            FirebaseAuth.getInstance(firebaseApp).apply {
                useEmulator(EMULATOR_HOST, 9099)
            }
        } else {
            FirebaseAuth.getInstance(firebaseApp)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp): FirebaseFirestore {
        return if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance(firebaseApp).apply {
                useEmulator(EMULATOR_HOST, 8080)
            }
        } else {
            return FirebaseFirestore.getInstance(firebaseApp)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseFunction(firebaseApp: FirebaseApp): FirebaseFunctions {
        return if (BuildConfig.DEBUG) {
            FirebaseFunctions.getInstance(firebaseApp).apply {
                useEmulator(EMULATOR_HOST, 5001)
            }
        } else {
            FirebaseFunctions.getInstance(firebaseApp)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(firebaseApp: FirebaseApp): FirebaseStorage {
        return if (BuildConfig.DEBUG) {
            FirebaseStorage.getInstance(firebaseApp).apply {
                useEmulator(EMULATOR_HOST, 9199)
            }
        } else {
            FirebaseStorage.getInstance(firebaseApp)
        }
    }

}
