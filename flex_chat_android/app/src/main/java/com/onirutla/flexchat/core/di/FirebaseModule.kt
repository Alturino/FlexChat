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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.onirutla.flexchat.BuildConfig
import com.onirutla.flexchat.core.util.FirebaseSecret.API_KEY
import com.onirutla.flexchat.core.util.FirebaseSecret.APPLICATION_ID
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
            .setApplicationId(APPLICATION_ID)
            .setApiKey(API_KEY)
            .setStorageBucket(STORAGE_BUCKET)
            .setProjectId(PROJECT_ID)
            .build()
        return FirebaseApp.initializeApp(context, firebaseOptions)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth = FirebaseAuth
        .getInstance(firebaseApp).apply {
            if (BuildConfig.DEBUG) {
                useEmulator("10.0.2.2", 9099)
            }
        }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(firebaseApp: FirebaseApp): FirebaseDatabase = FirebaseDatabase
        .getInstance(firebaseApp).apply {
            if (BuildConfig.DEBUG) {
                useEmulator("10.0.2.2", 9000)
            }
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp): FirebaseFirestore =
        FirebaseFirestore.getInstance(firebaseApp).apply {
            persistentCacheIndexManager?.apply { enableIndexAutoCreation() }
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings { setSizeBytes(500 * 1024 * 1024) })
            }
            if (BuildConfig.DEBUG) {
                useEmulator("10.0.2.2", 8080)
            }
        }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseFunction(firebaseApp: FirebaseApp): FirebaseFunctions = FirebaseFunctions
        .getInstance(firebaseApp).apply {
            if (BuildConfig.DEBUG) {
                useEmulator("10.0.2.2", 5001)
            }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(firebaseApp: FirebaseApp): FirebaseStorage = FirebaseStorage
        .getInstance(firebaseApp).apply {
            if (BuildConfig.DEBUG) {
                useEmulator("10.0.2.2", 9199)
            }
    }

}
