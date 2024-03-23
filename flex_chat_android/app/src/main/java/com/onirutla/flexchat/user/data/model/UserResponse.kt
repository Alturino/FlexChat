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

package com.onirutla.flexchat.user.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.onirutla.flexchat.user.domain.model.User
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Instant

@IgnoreExtraProperties
internal data class UserResponse(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val photoProfileUrl: String = "",
    val status: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)

internal fun UserResponse.toUser(conversationIds: List<String> = listOf()) = User(
    id = id,
    username = username,
    email = email,
    photoProfileUrl = photoProfileUrl,
    phoneNumber = phoneNumber,
    status = status,
    isOnline = isOnline,
    conversationIds = conversationIds,
    createdAt = createdAt?.toDate()?.toInstant()?.toKotlinInstant()?.toLocalDateTime(TimeZone.UTC)
        ?: Instant.now().toKotlinInstant().toLocalDateTime(TimeZone.UTC),
    updatedAt = updatedAt?.toDate()?.toInstant()?.toKotlinInstant()?.toLocalDateTime(TimeZone.UTC)
        ?: Instant.now().toKotlinInstant().toLocalDateTime(TimeZone.UTC),
    deletedAt = if (deletedAt == null) null else deletedAt.toDate().toInstant()?.toKotlinInstant()
        ?.toLocalDateTime(TimeZone.UTC)
)
