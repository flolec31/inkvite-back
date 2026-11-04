package com.stencilia.stenciliaback.appointment.service.implementation

import com.stencilia.stenciliaback.appointment.entity.Appointment
import com.stencilia.stenciliaback.appointment.exception.AppointmentBelongsToAnotherArtistException
import com.stencilia.stenciliaback.appointment.exception.AppointmentNotFoundException
import com.stencilia.stenciliaback.appointment.repository.AppointmentRepository
import com.stencilia.stenciliaback.appointment.service.AppointmentAccessService
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

    override fun findAppointmentForClient(clientId: UUID, appointmentId: UUID): Appointment {
        val appointment = appointmentRepository.findByIdAndVerifiedAtNotNull(appointmentId)
            .orElseThrow { AppointmentNotFoundException() }
        // A client only ever knows about their own appointments: treat someone
        // else's as non-existent rather than leaking that it exists.
        if (appointment.client.id != clientId) throw AppointmentNotFoundException()
        return appointment
    }
}
