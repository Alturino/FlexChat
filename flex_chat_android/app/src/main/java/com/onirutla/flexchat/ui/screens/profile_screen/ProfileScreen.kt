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

package com.onirutla.flexchat.ui.screens.profile_screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onEvent: (ProfileScreenEvent) -> Unit,
    onUiEvent: (ProfileScreenUiEvent) -> Unit,
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
            TopAppBar(
                title = { Text(text = stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = { onUiEvent(ProfileScreenUiEvent.OnNavigateUpClick) }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.wrapContentSize(unbounded = true),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape),
                    model = state.user.photoProfileUrl,
                    contentDescription = null,
                    loading = { CircularProgressIndicator() },
                    error = {
                        Icon(
                            modifier = Modifier
                                .size(250.dp)
                                .clip(CircleShape),
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                        )
                    }
                )
                IconButton(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.BottomEnd),
                    onClick = { onUiEvent(ProfileScreenUiEvent.OnChangeProfilePhotoClick) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                    )
                }
            }
            ListItem(
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                },
                headlineContent = {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = state.user.username
                    )
                },
                overlineContent = {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = stringResource(R.string.name)
                    )
                },
                trailingContent = {
                    IconButton(onClick = { onUiEvent(ProfileScreenUiEvent.OnChangeUsernameClick) }) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                    }
                },
            )
            ListItem(
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                },
                headlineContent = {},
                overlineContent = {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = stringResource(R.string.about)
                    )
                },
                supportingContent = {
                    Text(
                        text = state.user.status,
                        maxLines = 2,
                    )
                },
                trailingContent = {
                    IconButton(onClick = { onUiEvent(ProfileScreenUiEvent.OnChangeStatusClick) }) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                    }
                },
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(ProfileScreenEvent.OnLogOutClick) }) {
                Text(text = "Sign Out")
            }
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
private fun ProfileScreenPreview() {
    FlexChatTheme {
        ProfileScreen(state = ProfileScreenState(), onUiEvent = {}, onEvent = {})
    }
}
