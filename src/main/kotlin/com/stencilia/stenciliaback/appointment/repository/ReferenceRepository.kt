package com.stencilia.stenciliaback.appointment.repository

import com.stencilia.stenciliaback.appointment.entity.Reference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReferenceRepository : JpaRepository<Reference, UUID> {
    fun findByAppointmentId(appointmentId: UUID): List<Reference>
}
