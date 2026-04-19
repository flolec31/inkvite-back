package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AppointmentFormRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    @field:NotBlank @field:Size(min = 20) val description: String,
    @field:NotBlank val placement: String,
    @field:NotBlank val size: String,
    val firstTattoo: Boolean,
    val coverUp: Boolean,
    val references: List<ReferenceRequestDto> = emptyList()
) {

    @Suppress("unused")
    @AssertTrue(message = "If the tattoo is a cover, a photo is needed")
    fun isAtLeastOneReferenceIfCover(): Boolean = !coverUp || references.isNotEmpty()

    fun toModel(slug: String): AppointmentFormModel {
        return AppointmentFormModel(
            artistSlug = slug,
            clientEmail = email,
            clientFirstName = firstName,
            clientLastName = lastName,
            tattooDescription = description,
            tattooPlacement = placement,
            tattooSize = size,
            firstTattoo = firstTattoo,
            coverUp = coverUp,
            references = references.map { it.toModel() }
        )
    }
}