package com.stencilia.stenciliaback.auth.artist.event

data class PasswordResetEmailRequested(
    val to: String,
    val artistName: String,
    val token: String,
)
