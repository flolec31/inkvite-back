package com.stencilia.stenciliaback.auth.artist.event

data class ArtistVerificationEmailRequested(
    val to: String,
    val artistName: String,
    val token: String
)