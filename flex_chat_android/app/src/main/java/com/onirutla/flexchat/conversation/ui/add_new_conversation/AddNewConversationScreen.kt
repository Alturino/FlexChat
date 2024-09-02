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

package com.onirutla.flexchat.conversation.ui.add_new_conversation

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onirutla.flexchat.R
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.components.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddNewConversationScreen(
    modifier: Modifier = Modifier,
    state: AddNewConversationState,
    onEvent: (AddNewConversationEvent) -> Unit,
    onUiEvent: (AddNewConversationUiEvent) -> Unit,
) {
    LaunchedEffect(key1 = state.conversationId, block = {
        if (state.conversationId.isNotBlank() or state.conversationId.isNotEmpty()) {
            onUiEvent(AddNewConversationUiEvent.OnNavigateToConversationScreen(state.conversationId))
        }
    })
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
            TopAppBar(
                title = { Text(text = stringResource(R.string.select_available_users)) },
                navigationIcon = {
                    IconButton(onClick = { onUiEvent(AddNewConversationUiEvent.OnNavigateUpClick) }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            item {
                SearchField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    query = state.query,
                    onValueChange = { onEvent(AddNewConversationEvent.OnQueryChange(query = it)) },
                    onQueryClear = { onEvent(AddNewConversationEvent.OnQueryClear) },
                    onSearch = { onEvent(AddNewConversationEvent.OnSearchClick) }
                )
            }
            items(items = state.users) {
                ListItem(
                    modifier = Modifier.clickable {
                        onEvent(AddNewConversationEvent.OnUserItemClick(user = it))
                    },
                    headlineContent = { Text(text = it.username) },
                    leadingContent = {
                        SubcomposeAsyncImage(
                            modifier = Modifier.size(50.dp),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop,
                            model = it.photoProfileUrl,
                            contentDescription = null,
                            loading = { CircularProgressIndicator() }
                        )
                    },
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun AddNewConversationScreenPreview() {
    FlexChatTheme {
        AddNewConversationScreen(
            state = AddNewConversationState(),
            onUiEvent = {},
            onEvent = {}
        )
    }
}
