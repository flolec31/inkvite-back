package com.inkvite.inkviteback.auth.artist.event

data class PasswordChangedEmailRequested(
    val to: String,
    val artistName: String,
)
