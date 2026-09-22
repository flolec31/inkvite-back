package com.inkvite.inkviteback.discussion.service.implementation

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.event.NewMessageToArtistEmailRequested
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.discussion.service.AbstractDiscussionService
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.storage.service.StorageService
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
