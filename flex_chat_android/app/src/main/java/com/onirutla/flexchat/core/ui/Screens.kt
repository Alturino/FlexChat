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

package com.onirutla.flexchat.core.ui

sealed class Screens(val route: String) {
    data object AddNewConversationScreen : Screens("/conversation/add")
    data object ProfileScreen : Screens("/users")

    sealed class Conversation(val route: String){
        data object Default: Conversation("/conversations")
        data object ConversationList: Conversation("/conversations/list/")
        data object AddNewConversationScreen : Conversation("/conversation/add")
        data object CameraScreen: Conversation("/conversations/camera")
        data object EditPhotoScreen: Conversation("/conversations/camera/edit")
    }

    sealed class Auth(val route: String) {
        data object Default : Auth("/auth")
        data object LoginScreen : Auth("/auth/login")
        data object RegisterScreen : Auth("/auth/register")
        data object ForgotPassword : Auth("/auth/forgot")
    }
}
