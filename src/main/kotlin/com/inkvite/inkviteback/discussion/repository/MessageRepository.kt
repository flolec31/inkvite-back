package com.inkvite.inkviteback.discussion.repository

import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MessageRepository : JpaRepository<Message, UUID> {
    fun findByAppointmentIdOrderBySentAtAsc(appointmentId: UUID): List<Message>

    @Query(
        "SELECT DISTINCT m.appointment.id FROM Message m " +
            "WHERE m.appointment.id IN :appointmentIds AND m.sender = :sender AND m.readAt IS NULL"
    )
    fun findAppointmentIdsWithUnreadFrom(
        @Param("appointmentIds") appointmentIds: List<UUID>,
        @Param("sender") sender: MessageSender,
    ): Set<UUID>

    fun existsByAppointmentIdAndSenderAndReadAtIsNull(appointmentId: UUID, sender: MessageSender): Boolean
}
