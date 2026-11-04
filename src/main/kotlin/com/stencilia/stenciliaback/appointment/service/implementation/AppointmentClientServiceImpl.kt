package com.stencilia.stenciliaback.appointment.service.implementation

import com.stencilia.stenciliaback.appointment.dto.ClientAppointmentDetailsResponseDto
import com.stencilia.stenciliaback.appointment.dto.ReferenceDetailsResponseDto
import com.stencilia.stenciliaback.appointment.repository.ReferenceRepository
import com.stencilia.stenciliaback.appointment.service.AppointmentAccessService
import com.stencilia.stenciliaback.appointment.service.AppointmentClientService
import com.stencilia.stenciliaback.discussion.entity.MessageSender
import com.stencilia.stenciliaback.discussion.repository.MessageRepository
import com.stencilia.stenciliaback.storage.service.StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class AppointmentClientServiceImpl(
    private val storageService: StorageService,
    private val referenceRepository: ReferenceRepository,
    private val appointmentAccessService: AppointmentAccessService,
    private val messageRepository: MessageRepository,
) : AppointmentClientService {

    override fun getAppointmentDetails(
        clientId: UUID,
        appointmentId: UUID
    ): ClientAppointmentDetailsResponseDto {
        val appointment = appointmentAccessService.findAppointmentForClient(clientId, appointmentId)
        val references = referenceRepository.findByAppointmentId(appointment.id).map {
            ReferenceDetailsResponseDto(it, storageService.getSignedUrl(it.key))
        }
        val unreadMessages =
            messageRepository.existsByAppointmentIdAndSenderAndReadAtIsNull(appointment.id, MessageSender.ARTIST)
        return ClientAppointmentDetailsResponseDto(appointment, references, unreadMessages)
    }
}
