package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.client.entity.TattooClient
import jakarta.validation.Valid
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
    @field:Valid @field:Size(max = 10) val references: List<ReferenceRequestDto> = emptyList()
) {

    @Suppress("unused")
    @AssertTrue(message = "If the tattoo is a cover, a photo is needed")
    fun isAtLeastOneReferenceIfCover(): Boolean = !coverUp || references.isNotEmpty()

    fun toEntity(
        artist: TattooArtist,
        client: TattooClient
    ) : Appointment = Appointment(
        artist = artist,
        client = client,
        tattooDescription = this.description,
        tattooPlacement = this.placement,
        tattooSize = this.size,
        firstTattoo = this.firstTattoo,
        coverUp = this.coverUp
    )

}