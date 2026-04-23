package com.inkvite.inkviteback.appointment.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "reference")
class Reference(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "appointment_id", nullable = false)
    var appointment: Appointment,
    var key: String,
    var comment: String? = null,
)
