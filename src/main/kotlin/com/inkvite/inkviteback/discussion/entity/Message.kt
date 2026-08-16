package com.inkvite.inkviteback.discussion.entity

import com.inkvite.inkviteback.appointment.entity.Appointment
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "message")
class Message(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "appointment_id", nullable = false)
    var appointment: Appointment,
    @Enumerated(EnumType.STRING) var sender: MessageSender,
    var content: String? = null,
    var imageKey: String? = null,
    var sentAt: Instant = Instant.now(),
    var readAt: Instant? = null,
)
