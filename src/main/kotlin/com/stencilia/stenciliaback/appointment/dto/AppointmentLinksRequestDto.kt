package com.stencilia.stenciliaback.appointment.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AppointmentLinksRequestDto(
    @field:NotBlank @field:Email val email: String
)
