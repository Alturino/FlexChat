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

package com.onirutla.flexchat.auth.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.onirutla.flexchat.auth.domain.model.PasswordError
import com.onirutla.flexchat.core.util.isContainsAlphaNum
import com.onirutla.flexchat.core.util.isContainsDigit
import com.onirutla.flexchat.core.util.isContainsLowercase
import com.onirutla.flexchat.core.util.isContainsUppercase
import com.onirutla.flexchat.core.util.isLengthGreaterThanEqual

internal object PasswordValidationUseCase {
    operator fun invoke(password: String): Either<PasswordError, String> = either {
        ensure(password.isNotEmpty() or password.isNotBlank()) { raise(PasswordError.EmptyOrBlank) }
        ensure(password.isContainsLowercase()) { raise(PasswordError.NotContainsLowercase) }
        ensure(password.isContainsUppercase()) { raise(PasswordError.NotContainsUppercase) }
        ensure(password.isContainsAlphaNum()) { raise(PasswordError.NotContainNonAlphaNum) }
        ensure(password.isContainsDigit()) { raise(PasswordError.NotContainsDigit) }
        ensure(password.isLengthGreaterThanEqual()) { raise(PasswordError.TooShort) }
        password
    }
}
