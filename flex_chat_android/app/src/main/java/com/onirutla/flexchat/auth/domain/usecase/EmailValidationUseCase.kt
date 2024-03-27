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

import androidx.core.util.PatternsCompat
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.onirutla.flexchat.auth.domain.model.EmailError

internal object EmailValidationUseCase {
    operator fun invoke(email: String): Either<EmailError, String> = either {
        ensure(email.isNotBlank() or email.isNotEmpty()) {
            raise(EmailError.EmptyOrBlank)
        }
        ensure(PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            raise(EmailError.NotValidEmail)
        }
        email
    }
}