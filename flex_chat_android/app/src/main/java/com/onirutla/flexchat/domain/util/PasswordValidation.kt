/*
 * MIT License
 *
 * Copyright (c) 2023 - 2023 Ricky Alturino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.onirutla.flexchat.domain.util

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.onirutla.flexchat.domain.models.error_state.PasswordError

fun String.isValidPassword(): Either<PasswordError, String> = either {
    ensure(isNotEmpty() or isNotBlank()) { raise(PasswordError.EmptyOrBlank) }
    ensure(isContainsLowercase()) { raise(PasswordError.NotContainsLowercase) }
    ensure(isContainsUppercase()) { raise(PasswordError.NotContainsUppercase) }
    ensure(isContainsAlphaNum()) { raise(PasswordError.NotContainNonAlphaNum) }
    ensure(isContainsDigit()) { raise(PasswordError.NotContainsDigit) }
    ensure(isLengthGreaterThanEqual()) { raise(PasswordError.TooShort) }
    this@isValidPassword
}

fun String.isContainsDigit() = firstOrNull { it.isDigit() } != null
fun String.isContainsAlphaNum() = firstOrNull { !it.isLetterOrDigit() } != null
fun String.isContainsUppercase() = firstOrNull { it.isUpperCase() } != null
fun String.isLengthGreaterThanEqual(number: Int = 8) = length >= number
fun String.isContainsLowercase() = firstOrNull { it.isLowerCase() } != null
