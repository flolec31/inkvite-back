package com.inkvite.inkviteback.artist.dto

import com.inkvite.inkviteback.artist.model.TattooArtistProfileModel

data class ProfileResponseDto(
    val artistName: String,
    val slug: String,
    val profilePhotoUrl: String?,
) {
    constructor(tattooArtistProfileModel: TattooArtistProfileModel) : this(
        artistName = tattooArtistProfileModel.artistName,
        slug = tattooArtistProfileModel.slug,
        profilePhotoUrl = tattooArtistProfileModel.profilePhotoUrl,
    )
}