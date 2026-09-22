package com.inkvite.inkviteback.discussion.service

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.exception.InvalidMessageImageKeyException
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Audience-agnostic mechanics for the appointment message thread. Subclasses supply the
 * three things that differ between artist and client: how a subjectId is authorized against
 * an appointment ([resolveAppointment]), which [sender] stamps a new message, and which email
 * notification fires once one is posted ([onMessagePosted]).
 *
 * The subclasses are the Spring beans and carry the class-level `@Transactional(readOnly = true)`;
 * only the write path ([postMessage]) is annotated here.
 */
abstract class AbstractDiscussionService(
    protected val messageRepository: MessageRepository,
    protected val imageUploadService: ImageUploadService,
    protected val storageService: StorageService,
) : DiscussionService {

    protected abstract val sender: MessageSender

    protected abstract fun resolveAppointment(subjectId: UUID, appointmentId: UUID): Appointment

    protected abstract fun onMessagePosted(appointment: Appointment)

    override fun getMessages(subjectId: UUID, appointmentId: UUID): List<MessageResponseDto> {
        resolveAppointment(subjectId, appointmentId)
        return messageRepository.findByAppointmentIdOrderBySentAtAsc(appointmentId)
            .map { MessageResponseDto(it, it.imageKey?.let(storageService::getSignedUrl)) }
    }

    override fun uploadMessageImage(
        subjectId: UUID,
        appointmentId: UUID,
        image: MultipartFile,
    ): ImageUploadResponseDto {
        resolveAppointment(subjectId, appointmentId)
        return imageUploadService.uploadMessageImage(subjectId, image)
    }

    @Transactional
    override fun postMessage(
        subjectId: UUID,
        appointmentId: UUID,
        content: String?,
        imageKey: String?,
    ): MessageResponseDto {
        val appointment = resolveAppointment(subjectId, appointmentId)
        val normalizedContent = content?.takeIf { it.isNotBlank() }
        val normalizedImageKey = imageKey?.takeIf { it.isNotBlank() }
        if (normalizedImageKey != null && !normalizedImageKey.startsWith("messages/$subjectId/")) {
            throw InvalidMessageImageKeyException()
        }
        val message = messageRepository.save(
            Message(
                appointment = appointment,
                sender = sender,
                content = normalizedContent,
                imageKey = normalizedImageKey,
            )
        )
        onMessagePosted(appointment)
        return MessageResponseDto(message, normalizedImageKey?.let(storageService::getSignedUrl))
    }
}
