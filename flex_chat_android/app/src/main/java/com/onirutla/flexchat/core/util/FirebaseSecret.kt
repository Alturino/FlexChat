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

package com.onirutla.flexchat.core.util

import com.onirutla.flexchat.BuildConfig

object FirebaseSecret {
    const val API_KEY = BuildConfig.API_KEY
    const val APP_ID = BuildConfig.APP_ID
    const val STORAGE_BUCKET = BuildConfig.STORAGE_BUCKET
    const val PROJECT_ID = BuildConfig.PROJECT_ID
    const val SIGN_IN_CLIENT_SERVER_CLIENT_ID = BuildConfig.SIGN_IN_CLIENT_SERVER_CLIENT_ID
    const val EMULATOR_HOST = "192.168.1.5"
}
