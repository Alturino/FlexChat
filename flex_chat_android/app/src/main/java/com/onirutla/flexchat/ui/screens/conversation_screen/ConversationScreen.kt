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

package com.onirutla.flexchat.ui.screens.conversation_screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onirutla.flexchat.domain.models.Conversation
import com.onirutla.flexchat.domain.models.Message
import com.onirutla.flexchat.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.util.toLocalTimeString
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
    state: ConversationScreenState,
    onEvent: (ConversationScreenEvent) -> Unit,
    onUiEvent: (ConversationScreenUiEvent) -> Unit,
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
                    IconButton(onClick = { onUiEvent(ConversationScreenUiEvent.OnNavigateUp) }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
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
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    value = state.draftMessage,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(onSend = { onEvent(ConversationScreenEvent.OnDraftMessageSend) }),
                    onValueChange = { onEvent(ConversationScreenEvent.OnDraftMessageChanged(it)) },
                    trailingIcon = {
                        IconButton(onClick = { onEvent(ConversationScreenEvent.OnDraftMessageSend) }) {
                            Icon(imageVector = Icons.Rounded.Send, contentDescription = null)
                        }
                    }
                )
            }
            items(items = state.messages) {
                ListItem(
                    leadingContent = {
                        SubcomposeAsyncImage(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            model = it.senderPhotoUrl,
                            contentDescription = null,
                            loading = { CircularProgressIndicator(modifier = Modifier.size(50.dp)) },
                            error = {
                                Icon(
                                    modifier = Modifier.size(50.dp),
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                )
                            }
                        )
                    },
                    headlineContent = { Text(text = it.senderName) },
                    supportingContent = { Text(text = it.messageBody) },
                    trailingContent = { Text(text = it.createdAt.toLocalTimeString()) }
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
        ConversationScreen(
            state = ConversationScreenState(
                conversation = Conversation(
                    id = "aeque",
                    conversationName = "Pesarkas Handal",
                    imageUrl = "https://duckduckgo.com/?q=persius",
                    isGroup = false,
                    conversationMembers = listOf(),
                    latestMessage = Message(),
                    createdAt = LocalDateTime.now(ZoneId.systemDefault()),
                    deletedAt = LocalDateTime.now(ZoneId.systemDefault()),
                ),
                messages = listOf(
                    Message(
                        id = "delenit",
                        conversationId = "salutatus",
                        conversationMemberId = "rhoncus",
                        userId = "conubia",
                        senderName = "Robbie Combs",
                        senderPhotoUrl = "https://www.google.com/#q=solet",
                        messageBody = "ius",
                        createdAt = LocalDateTime.now(ZoneId.systemDefault()),
                        deletedAt = LocalDateTime.now(ZoneId.systemDefault())
                    )
                )
            ),
            onEvent = {},
            onUiEvent = {},
        )
    }
}
