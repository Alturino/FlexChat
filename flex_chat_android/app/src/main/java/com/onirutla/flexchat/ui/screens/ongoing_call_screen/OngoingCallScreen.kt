package com.onirutla.flexchat.ui.screens.ongoing_call_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun OngoingCallScreen(modifier: Modifier = Modifier) {
    var isNotMute by remember { mutableStateOf(true) }
    var isMicOn by remember { mutableStateOf(true) }
    var isVideoCamOn by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(
            left = 16.dp,
            top = 16.dp,
            right = 16.dp,
            bottom = 16.dp
        )
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Someone is calling",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calling",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                FilledTonalIconToggleButton(
                    checked = isNotMute,
                    onCheckedChange = { isNotMute = it },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    if (isNotMute) {
                        Icon(imageVector = Icons.Rounded.VolumeUp, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Rounded.VolumeOff, contentDescription = null)
                    }
                }
                FilledTonalIconToggleButton(
                    checked = isVideoCamOn,
                    onCheckedChange = { isVideoCamOn = it },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    if (isVideoCamOn) {
                        Icon(imageVector = Icons.Rounded.Videocam, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Rounded.VideocamOff, contentDescription = null)
                    }
                }
                FilledTonalIconToggleButton(
                    checked = isMicOn,
                    onCheckedChange = { isMicOn = it },
                    colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (!isMicOn) {
                        Icon(imageVector = Icons.Rounded.MicOff, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Rounded.Mic, contentDescription = null)
                    }
                }
                FilledTonalIconButton(
                    onClick = { /*TODO*/ },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.CallEnd, contentDescription = null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun OutgoingCallScreenPreview() {
    FlexChatTheme {
        OngoingCallScreen()
    }
}
