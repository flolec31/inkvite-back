package com.inkvite.inkviteback.discussion.service

import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface DiscussionService {
    fun getMessages(artistId: UUID, appointmentId: UUID): List<MessageResponseDto>
    fun postMessage(artistId: UUID, appointmentId: UUID, content: String?, imageKey: String?): MessageResponseDto
    fun uploadMessageImage(artistId: UUID, appointmentId: UUID, image: MultipartFile): ImageUploadResponseDto
}
