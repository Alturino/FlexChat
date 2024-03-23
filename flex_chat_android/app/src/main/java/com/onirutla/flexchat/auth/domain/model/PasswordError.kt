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

package com.onirutla.flexchat.auth.domain.model

internal sealed class PasswordError(val message: String) {
    data object TooShort : PasswordError(message = "Password should be at least more than or equal to 8 characters")
    data object NotContainsDigit : PasswordError(message = "Password should be at least have 1 digit number")
    data object NotContainsUppercase : PasswordError(message = "Password should be at least have 1 letter uppercase")
    data object NotContainsLowercase : PasswordError(message = "Password should be at least have 1 letter lowercase")
    data object NotContainNonAlphaNum: PasswordError(message = "Password should be at least have 1 character non alpha numeric")
    data object EmptyOrBlank : PasswordError(message = "Password should not be empty or blank")
    data object PasswordNotTheSameWithConfirmationPassword: PasswordError(message = "Password not the same with confirmation password")
    data object ConfirmationPasswordNotTheSameWithPassword: PasswordError(message = "Confirmation password not the same with password")
}
