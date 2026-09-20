package com.inkvite.inkviteback.discussion.service

import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import java.util.UUID

fun interface DiscussionClientService {
    fun getMessages(clientId: UUID, appointmentId: UUID): List<MessageResponseDto>
}
