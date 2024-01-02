/*
 * MIT License
 *
 * Copyright (c) 2023 Ricky Alturino
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

package com.onirutla.flexchat.core.data.models

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.onirutla.flexchat.domain.models.User
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@IgnoreExtraProperties
data class UserResponse(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val photoProfileUrl: String = "",
    val status: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    @ServerTimestamp
    val deletedAt: Date? = null,
)

fun UserResponse.toUser() = User(
    id = id,
    username = username,
    email = email,
    photoProfileUrl = photoProfileUrl,
    phoneNumber = phoneNumber,
    status = status,
    isOnline = isOnline,
    conversation = listOf(),
    createdAt = LocalDateTime.ofInstant(
        createdAt?.toInstant() ?: Date().toInstant(),
        ZoneId.systemDefault()
    ),
    deletedAt = LocalDateTime.ofInstant(
        createdAt?.toInstant() ?: Date().toInstant(),
        ZoneId.systemDefault()
    ),
)