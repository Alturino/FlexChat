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

data class RegisterScreenState(
    val username: String = "",
    val email: String = "",
    val isEmailError: Boolean? = null,
    val emailErrorMessage: String = "",
    val password: String = "",
    val isPasswordError: Boolean? = null,
    val passwordErrorMessage: String = "",
    val isPasswordVisible: Boolean = false,
    val confirmPassword: String = "",
    val confirmPasswordErrorMessage: String = "",
    val isConfirmPasswordError: Boolean? = null,
    val isConfirmPasswordVisible: Boolean = false,
    val isRegisterButtonEnabled: Boolean? = null,
    val isRegisterSuccessful: Boolean? = null,
    val registerErrorMessage: String = "",
    val isRegisterWithGoogleSuccessful: Boolean? = null,
    val registerWithGoogleErrorMessage: String = "",
)
