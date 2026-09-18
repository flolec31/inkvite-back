package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.dto.ClientAppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceDetailsResponseDto
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.appointment.service.AppointmentClientService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class AppointmentClientServiceImpl(
    private val storageService: StorageService,
    private val referenceRepository: ReferenceRepository,
    private val appointmentAccessService: AppointmentAccessService,
) : AppointmentClientService {

    override fun getAppointmentDetails(
        clientId: UUID,
        appointmentId: UUID
    ): ClientAppointmentDetailsResponseDto {
        val appointment = appointmentAccessService.findAppointmentForClient(clientId, appointmentId)
        val references = referenceRepository.findByAppointmentId(appointment.id).map {
            ReferenceDetailsResponseDto(it, storageService.getSignedUrl(it.key))
        }
        return ClientAppointmentDetailsResponseDto(appointment, references)
    }
}
