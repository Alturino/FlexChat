package com.onirutla.flexchat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onirutla.flexchat.domain.models.Message
import com.onirutla.flexchat.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.util.toLocalTimeString
import java.time.LocalDateTime

@Composable
fun ChatBubbleItem(modifier: Modifier = Modifier, message: Message, isMySelf: Boolean) {
    Column(modifier = modifier.fillMaxWidth()) {
        ElevatedCard(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(fraction = 0.8f)
                .align(if (isMySelf) Alignment.End else Alignment.Start),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (isMySelf) 24.dp else 0.dp,
                bottomEnd = if (isMySelf) 0.dp else 24.dp
            ),
            elevation = CardDefaults.elevatedCardElevation(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = message.messageBody,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                    softWrap = true,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = message.createdAt.toLocalTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ChatBubblePreview() {
    FlexChatTheme {
        ChatBubbleItem(
            message = Message(
                id = "ornare",
                conversationId = "suas",
                conversationMemberId = "suspendisse",
                userId = "viris",
                senderName = "Mauricio Barron",
                senderPhotoUrl = "https://www.google.com/#q=elitr",
                messageBody = "lacusfadsfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdffasdfadf",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.MAX,
                deletedAt = LocalDateTime.MAX
            ),
            isMySelf = true
        )
    }
}
