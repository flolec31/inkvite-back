package com.inkvite.inkviteback.discussion.service

import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import java.util.UUID

interface DiscussionService {
    fun getMessages(artistId: UUID, appointmentId: UUID): List<MessageResponseDto>
    fun postMessage(artistId: UUID, appointmentId: UUID, content: String): MessageResponseDto
}
