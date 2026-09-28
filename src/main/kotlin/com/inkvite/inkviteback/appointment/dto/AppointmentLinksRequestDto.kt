package com.inkvite.inkviteback.appointment.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AppointmentLinksRequestDto(
    @field:NotBlank @field:Email val email: String
)
