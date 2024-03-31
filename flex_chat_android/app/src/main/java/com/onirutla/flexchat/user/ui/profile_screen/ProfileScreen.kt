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

package com.onirutla.flexchat.user.ui.profile_screen

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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onirutla.flexchat.R
import com.onirutla.flexchat.core.ui.theme.FlexChatTheme

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
                    filterQuality = FilterQuality.High,
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
