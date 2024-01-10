package com.onirutla.flexchat.core.data.models

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class AttachmentResponse(
    val id: String,
    val userId: String,
    val conversationId: String,
    val messageId: String,
    val url: String,
    val name: String,
    val mimeType: String,
    val createdAt: Date? = null,
    val deletedAt: Date? = null,
)
