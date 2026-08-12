package com.inkvite.inkviteback.discussion.dto

import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import java.time.Instant
import java.util.UUID

data class MessageResponseDto(
    val id: UUID,
    val sender: MessageSender,
    val content: String,
    val sentAt: Instant,
    val readAt: Instant?,
) {
    constructor(message: Message) : this(
        id = message.id,
        sender = message.sender,
        content = message.content,
        sentAt = message.sentAt,
        readAt = message.readAt,
    )
}
