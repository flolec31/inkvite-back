package com.inkvite.inkviteback.appointment.repository

import com.inkvite.inkviteback.appointment.entity.Appointment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface AppointmentRepository : JpaRepository<Appointment, UUID> {
    fun findByArtistIdAndVerifiedAtNotNull(artistId: UUID, pageable: Pageable): Page<Appointment>
    fun findByIdAndVerifiedAtNotNull(appointmentId: UUID): Optional<Appointment>
}
