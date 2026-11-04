package com.stencilia.stenciliaback.discussion.service.implementation

import com.stencilia.stenciliaback.appointment.entity.Appointment
import com.stencilia.stenciliaback.appointment.service.AppointmentAccessService
import com.stencilia.stenciliaback.discussion.entity.MessageSender
import com.stencilia.stenciliaback.discussion.event.NewMessageToArtistEmailRequested
import com.stencilia.stenciliaback.discussion.repository.MessageRepository
import com.stencilia.stenciliaback.discussion.service.AbstractDiscussionService
import com.stencilia.stenciliaback.storage.service.ImageUploadService
import com.stencilia.stenciliaback.storage.service.StorageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DiscussionClientServiceImpl(
    private val appointmentAccessService: AppointmentAccessService,
    messageRepository: MessageRepository,
    imageUploadService: ImageUploadService,
    storageService: StorageService,
    private val eventPublisher: ApplicationEventPublisher,
) : AbstractDiscussionService(messageRepository, imageUploadService, storageService) {

    override val sender = MessageSender.CLIENT

    override fun resolveAppointment(subjectId: UUID, appointmentId: UUID): Appointment =
        appointmentAccessService.findAppointmentForClient(subjectId, appointmentId)

    override fun onMessagePosted(appointment: Appointment) {
        eventPublisher.publishEvent(NewMessageToArtistEmailRequested(appointment))
    }
}
