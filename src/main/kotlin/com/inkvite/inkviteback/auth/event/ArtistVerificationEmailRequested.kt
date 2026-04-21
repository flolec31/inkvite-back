package com.inkvite.inkviteback.auth.event

data class ArtistVerificationEmailRequested(
    val to: String,
    val token: String
)