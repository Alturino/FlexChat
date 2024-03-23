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

package com.onirutla.flexchat.auth.register.ui

sealed interface RegisterScreenEvent {
    data class OnUsernameChange(val username: String) : RegisterScreenEvent
    data class OnEmailChange(val email: String) : RegisterScreenEvent
    data class OnPasswordChange(val password: String) : RegisterScreenEvent
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterScreenEvent
    data class OnIsPasswordVisibleChange(val isPasswordVisible: Boolean) : RegisterScreenEvent
    data class OnIsConfirmPasswordVisibleChange(val isPasswordVisible: Boolean) :
        RegisterScreenEvent
    data object OnRegisterClick : RegisterScreenEvent
    data object OnRegisterWithGoogleClick : RegisterScreenEvent
}
