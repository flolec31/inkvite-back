package com.inkvite.inkviteback.auth.artist.event

data class ArtistVerificationEmailRequested(
    val to: String,
    val artistName: String,
    val token: String
)