package com.stencilia.stenciliaback.auth.artist.event

data class PasswordChangedEmailRequested(
    val to: String,
    val artistName: String,
)
