package com.inkvite.inkviteback.auth.event

data class PasswordResetEmailRequested(
    val to: String,
    val artistName: String,
    val token: String,
)
