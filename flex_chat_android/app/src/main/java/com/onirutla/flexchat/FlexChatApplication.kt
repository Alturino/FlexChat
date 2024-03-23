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

package com.onirutla.flexchat

import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FlexChatApplication : MultiDexApplication(), ImageLoaderFactory, CameraXConfig.Provider {

    @Inject
    lateinit var coilImageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(): ImageLoader = coilImageLoader

    override fun getCameraXConfig(): CameraXConfig = CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
        .setCameraExecutor(ContextCompat.getMainExecutor(this))
        .setMinimumLoggingLevel(Log.DEBUG)
        .build()
}
