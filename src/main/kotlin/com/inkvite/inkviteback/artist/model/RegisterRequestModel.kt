package com.inkvite.inkviteback.artist.model

import com.inkvite.inkviteback.artist.entity.TattooArtist
import java.time.Instant
import java.util.UUID

data class RegisterRequestModel(
    val email: String,
    val encodedPassword: String,
    val artistName: String,
    val slug: String,
    val city: String,
    val countryCode: String
) {
    fun toEntity(id: UUID): TattooArtist = TattooArtist(
        id = id,
        email = email,
        password = encodedPassword,
        artistName = artistName,
        slug = slug,
        city = city,
        countryCode = countryCode,
        registeredAt = Instant.now(),
    )
}
