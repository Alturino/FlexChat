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

package com.onirutla.flexchat.conversation.ui.conversations

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConversationsScreen(
    modifier: Modifier = Modifier,
    state: ConversationsState,
    onUiEvent: (ConversationsUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding(),
        contentWindowInsets = WindowInsets(
            left = 16.dp,
            top = 16.dp,
            right = 16.dp,
            bottom = 16.dp
        ),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(left = 16.dp, right = 16.dp),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = { onUiEvent(ConversationsUiEvent.OnProfileIconClick) },
                        content = {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onUiEvent(ConversationsUiEvent.OnFloatingActionButtonClick) }) {
                Icon(imageVector = Icons.Rounded.AddComment, contentDescription = null)
            }
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
//            items(
//                items = state.conversations,
//            ) {
//                ConversationItem(
//                    modifier = Modifier.fillMaxWidth(),
//                    conversation = it,
//                    onClick = { onUiEvent(ConversationsUiEvent.OnConversationClick(it)) }
//                )
//            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun MainScreenPreview() {
    FlexChatTheme {
        ConversationsScreen(
            state = ConversationsState(
                conversations = listOf(
                    Conversation(
                        id = "dictumst",
                        conversationName = "Cornell Glover",
                        imageUrl = "https://duckduckgo.com/?q=dico",
                        isGroup = false,
//                        latestMessage = Message(),
                        createdAt = Timestamp.now(),
                        deletedAt = Timestamp.now()
                    ),
//                    Conversation(
//                        id = "dictumst",
//                        name = "Cornell Glover",
//                        imageUrl = "https://duckduckgo.com/?q=dico",
//                        isGroup = false,
//                        conversationMembers = listOf(),
//                        latestMessage = "his",
//                        users = listOf(),
//                        createdAt = "ridiculus",
//                        deletedAt = "sed"
//                    )
                )
            ),
            onUiEvent = {},
        )
    }
}
