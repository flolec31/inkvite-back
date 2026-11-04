package com.stencilia.stenciliaback.artist.dto

import com.stencilia.stenciliaback.artist.entity.TattooArtist

data class ProfileResponseDto(
    val artistName: String,
    val slug: String,
    val city: String,
    val countryCode: String,
    val profilePhotoUrl: String?,
    val agendaOpen: Boolean,
) {
    constructor(tattooArtist: TattooArtist, profilePhotoUrl: String?) : this(
        artistName = tattooArtist.artistName,
        slug = tattooArtist.slug,
        city = tattooArtist.city,
        countryCode = tattooArtist.countryCode,
        profilePhotoUrl = profilePhotoUrl,
        agendaOpen = tattooArtist.agendaOpen
    )
}