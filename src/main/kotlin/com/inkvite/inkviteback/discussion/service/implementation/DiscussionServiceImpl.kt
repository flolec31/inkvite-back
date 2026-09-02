package com.inkvite.inkviteback.discussion.service.implementation

import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.event.NewMessageEmailRequested
import com.inkvite.inkviteback.discussion.exception.InvalidMessageImageKeyException
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.discussion.service.DiscussionService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DiscussionServiceImpl(
    private val appointmentAccessService: AppointmentAccessService,
    private val messageRepository: MessageRepository,
    private val imageUploadService: ImageUploadService,
    private val storageService: StorageService,
    private val eventPublisher: ApplicationEventPublisher,
) : DiscussionService {

    override fun getMessages(artistId: UUID, appointmentId: UUID): List<MessageResponseDto> {
        appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        return messageRepository.findByAppointmentIdOrderBySentAtAsc(appointmentId)
            .map { MessageResponseDto(it, it.imageKey?.let(storageService::getSignedUrl)) }
    }

    override fun uploadMessageImage(
        artistId: UUID,
        appointmentId: UUID,
        image: MultipartFile,
    ): ImageUploadResponseDto {
        appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        return imageUploadService.uploadMessageImage(artistId, image)
    }

    @Transactional
    override fun postMessage(
        artistId: UUID,
        appointmentId: UUID,
        content: String?,
        imageKey: String?,
    ): MessageResponseDto {
        val appointment = appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        val normalizedContent = content?.takeIf { it.isNotBlank() }
        val normalizedImageKey = imageKey?.takeIf { it.isNotBlank() }
        if (normalizedImageKey != null && !normalizedImageKey.startsWith("messages/$artistId/")) {
            throw InvalidMessageImageKeyException()
        }
        val message = messageRepository.save(
            Message(
                appointment = appointment,
                sender = MessageSender.ARTIST,
                content = normalizedContent,
                imageKey = normalizedImageKey,
            )
        )
        eventPublisher.publishEvent(NewMessageEmailRequested(appointment))
        return MessageResponseDto(message, normalizedImageKey?.let(storageService::getSignedUrl))
    }
}
