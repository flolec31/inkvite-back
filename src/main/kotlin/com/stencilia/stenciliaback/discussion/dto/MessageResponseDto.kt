package com.stencilia.stenciliaback.discussion.dto

import com.stencilia.stenciliaback.discussion.entity.Message
import com.stencilia.stenciliaback.discussion.entity.MessageSender
import java.time.Instant
import java.util.UUID

data class MessageResponseDto(
    val id: UUID,
    val sender: MessageSender,
    val content: String?,
    val imageUrl: String?,
    val sentAt: Instant,
    val readAt: Instant?,
) {
    constructor(message: Message, imageUrl: String?) : this(
        id = message.id,
        sender = message.sender,
        content = message.content,
        imageUrl = imageUrl,
        sentAt = message.sentAt,
        readAt = message.readAt,
    )
}
