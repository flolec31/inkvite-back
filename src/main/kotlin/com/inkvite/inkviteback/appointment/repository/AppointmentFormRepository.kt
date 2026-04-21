package com.inkvite.inkviteback.appointment.repository

import com.inkvite.inkviteback.appointment.entity.AppointmentForm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AppointmentFormRepository : JpaRepository<AppointmentForm, UUID>
