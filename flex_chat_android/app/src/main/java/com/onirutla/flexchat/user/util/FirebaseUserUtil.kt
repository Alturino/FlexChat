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

package com.onirutla.flexchat.user.util

import com.google.firebase.auth.FirebaseUser
import com.onirutla.flexchat.user.data.model.UserResponse
import com.onirutla.flexchat.user.domain.model.User
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun FirebaseUser.toUser(conversationIds: List<String> = listOf()) = User(
    id = uid,
    email = email.orEmpty(),
    conversationIds = conversationIds,
    isOnline = false,
    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    username = displayName.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    photoProfileUrl = photoUrl.toString(),
    status = "Welcome to FlexChat"
)

internal fun FirebaseUser.toUserResponse(): UserResponse = UserResponse(
    id = uid,
    email = email.orEmpty(),
    isOnline = false,
    createdAt = null,
    username = displayName.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    photoProfileUrl = photoUrl.toString(),
    status = "Welcome to FlexChat"
)