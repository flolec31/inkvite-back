package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.exception.AppointmentBelongsToAnotherArtistException
import com.inkvite.inkviteback.appointment.exception.AppointmentNotFoundException
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AppointmentAccessServiceImpl(
    private val appointmentRepository: AppointmentRepository,
) : AppointmentAccessService {

    override fun findOwnedAppointment(artistId: UUID, appointmentId: UUID): Appointment {
        val appointment = appointmentRepository.findByIdAndVerifiedAtNotNull(appointmentId)
            .orElseThrow { AppointmentNotFoundException() }
        if (appointment.artist.id != artistId) throw AppointmentBelongsToAnotherArtistException()
        return appointment
    }
}
