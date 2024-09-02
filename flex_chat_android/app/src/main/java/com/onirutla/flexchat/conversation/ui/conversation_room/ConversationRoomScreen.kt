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

package com.onirutla.flexchat.conversation.ui.conversation_room

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.conversation.data.model.Message
import com.onirutla.flexchat.core.ui.components.ChatBubbleItem
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.components.DraftMessageField
import com.onirutla.flexchat.user.data.model.User

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ConversationRoomScreen(
    modifier: Modifier = Modifier,
    state: ConversationRoomState,
    onEvent: (ConversationRoomEvent) -> Unit,
    onUiEvent: (ConversationRoomUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(text = state.conversation.conversationName) },
                navigationIcon = {
                    IconButton(onClick = { onUiEvent(ConversationRoomUiEvent.OnNavigateUp) }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { onUiEvent(ConversationRoomUiEvent.OnCallClick) }) {
                            Icon(imageVector = Icons.Rounded.Call, contentDescription = null)
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(
            left = 16.dp,
            top = 16.dp,
            right = 16.dp,
            bottom = 16.dp
        )
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DraftMessageField(
                        modifier = Modifier.fillParentMaxWidth(fraction = 0.85f),
                        message = state.draftMessage,
                        onDraftMessageChanged = {
                            onEvent(ConversationRoomEvent.OnDraftMessageChanged(it))
                        },
                        onDraftMessageSend = { onEvent(ConversationRoomEvent.OnDraftMessageSend) },
                        onEmojiClick = { onUiEvent(ConversationRoomUiEvent.OnEmojiClick) },
                        onAttachmentClick = { onUiEvent(ConversationRoomUiEvent.OnAttachmentClick) },
                        onCameraClick = { onUiEvent(ConversationRoomUiEvent.OnCameraClick) },
                    )
                    FilledTonalIconButton(
                        onClick = { onEvent(ConversationRoomEvent.OnDraftMessageSend) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = null
                        )
                    }
                }
            }
            items(items = state.messages, key = { it.id }) {
                ChatBubbleItem(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMedium,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ), message = it, isMySelf = it.userId == state.currentUser.id
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true
)
@Composable
private fun ConversationScreenPreview() {
    FlexChatTheme {
        ConversationRoomScreen(
            state = ConversationRoomState(
                currentUser = User(id = "conubia"),
                conversation = Conversation(
                    id = "aeque",
                    conversationName = "Pesarkas Handal",
                    imageUrl = "https://duckduckgo.com/?q=persius",
                    isGroup = false,
//                    latestMessage = Message(),
                    createdAt = Timestamp.now(),
                    deletedAt = Timestamp.now(),
                    slug = "",
                ),
                messages = listOf(
                    Message(
                        id = "delenit",
                        conversationId = "salutatus",
                        userId = "conubia",
                        senderName = "Robbie Combs",
                        senderPhotoUrl = "https://www.google.com/#q=solet",
                        messageBody = "ius",
                        createdAt = Timestamp.now(),
                        deletedAt = Timestamp.now()
                    ),
                    Message(
                        id = "asdfasdfasdf",
                        conversationId = "salutatus",
                        userId = "test",
                        senderName = "Robbie Combs",
                        senderPhotoUrl = "https://www.google.com/#q=solet",
                        messageBody = "ius",
                        createdAt = Timestamp.now(),
                        deletedAt = Timestamp.now()
                    )
                )
            ),
            onEvent = {},
            onUiEvent = {},
        )
    }
}
