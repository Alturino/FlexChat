package com.onirutla.flexchat.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.onirutla.flexchat.core.data.models.Conversation
import com.onirutla.flexchat.ui.theme.FlexChatTheme

@Composable
fun ConversationItem(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = conversation.name) },
        leadingContent = {
            SubcomposeAsyncImage(
                modifier = Modifier.size(50.dp),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                model = conversation.imageUrl,
                contentDescription = null,
                loading = { CircularProgressIndicator() }
            )
        },
        supportingContent = {
            Text(text = conversation.latestMessage)
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ConversationItemPreview() {
    FlexChatTheme {
        ConversationItem(
            conversation = Conversation(
                id = "voluptatibus",
                name = "Duncan Bruce",
                imageUrl = "https://www.google.com/#q=errem",
                isGroup = false,
                conversationMembers = listOf(),
                latestMessage = "assueverit",
                createdAt = "semper",
                deletedAt = "sem"
            ),
            onClick = {}
        )
    }
}
