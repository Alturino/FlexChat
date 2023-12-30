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

package com.onirutla.flexchat.ui.screens.add_new_conversation_screen

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
import com.onirutla.flexchat.ui.components.SearchField
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewConversationScreen(
    modifier: Modifier = Modifier,
    state: AddNewConversationScreenState,
    onEvent: (AddNewConversationScreenEvent) -> Unit,
    onUiEvent: (AddNewConversationScreenUiEvent) -> Unit,
) {
    LaunchedEffect(key1 = state.conversationId, block = {
        if (state.conversationId.isNotBlank() or state.conversationId.isNotEmpty()) {
            onUiEvent(AddNewConversationScreenUiEvent.OnNavigateToConversationScreen(state.conversationId))
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
                    IconButton(onClick = { onUiEvent(AddNewConversationScreenUiEvent.OnNavigateUpClick) }) {
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
                    onValueChange = { onEvent(AddNewConversationScreenEvent.OnQueryChange(query = it)) },
                    onQueryClear = { onEvent(AddNewConversationScreenEvent.OnQueryClear) },
                    onSearch = { onEvent(AddNewConversationScreenEvent.OnSearchClick) }
                )
            }
            items(items = state.users) {
                ListItem(
                    modifier = Modifier.clickable {
                        onEvent(AddNewConversationScreenEvent.OnUserItemClick(user = it))
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
            state = AddNewConversationScreenState(),
            onUiEvent = {},
            onEvent = {}
        )
    }
}
