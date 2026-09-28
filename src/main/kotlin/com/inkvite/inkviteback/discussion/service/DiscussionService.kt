package com.inkvite.inkviteback.discussion.service

import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Appointment-scoped message thread, shared by both audiences. `subjectId` is the
 * caller's id (a `TattooArtist` or a `TattooClient`), taken from the JWT subject.
 */
interface DiscussionService {
    fun getMessages(subjectId: UUID, appointmentId: UUID): List<MessageResponseDto>
    fun postMessage(subjectId: UUID, appointmentId: UUID, content: String?, imageKey: String?): MessageResponseDto
    fun uploadMessageImage(subjectId: UUID, appointmentId: UUID, image: MultipartFile): ImageUploadResponseDto
}
