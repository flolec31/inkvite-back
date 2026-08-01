package com.inkvite.inkviteback.artist.dto

import com.inkvite.inkviteback.artist.entity.TattooArtist

data class ProfileResponseDto(
    val artistName: String,
    val slug: String,
    val city: String,
    val countryCode: String,
    val profilePhotoUrl: String?,
) {
    constructor(tattooArtist: TattooArtist, profilePhotoUrl: String?) : this(
        artistName = tattooArtist.artistName,
        slug = tattooArtist.slug,
        city = tattooArtist.city,
        countryCode = tattooArtist.countryCode,
        profilePhotoUrl = profilePhotoUrl
    )
}