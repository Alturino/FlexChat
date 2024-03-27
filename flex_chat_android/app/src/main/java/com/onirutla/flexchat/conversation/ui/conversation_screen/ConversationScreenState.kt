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

package com.onirutla.flexchat.conversation.ui.conversation_screen

import com.onirutla.flexchat.conversation.domain.model.Conversation
import com.onirutla.flexchat.conversation.domain.model.ConversationMember
import com.onirutla.flexchat.conversation.domain.model.Message
import com.onirutla.flexchat.user.domain.model.User

data class ConversationScreenState(
    val conversation: Conversation = Conversation(),
    val messages: List<Message> = listOf(),
    val currentUser: User = User(),
    val currentConversationMember: ConversationMember = ConversationMember(),
    val draftMessage: String = "",
    val isSendMessageError: Boolean = false,
    val sendErrorMessage: String = "",
)
