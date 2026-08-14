package com.inkvite.inkviteback.discussion.repository

import com.inkvite.inkviteback.discussion.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByAppointmentIdOrderBySentAtAsc(appointmentId: UUID): List<Message>
}
