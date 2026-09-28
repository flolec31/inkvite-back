package com.inkvite.inkviteback.auth.artist.event

data class PasswordResetEmailRequested(
    val to: String,
    val artistName: String,
    val token: String,
)
