package com.inkvite.inkviteback.auth.event

data class PasswordChangedEmailRequested(
    val to: String,
    val artistName: String,
)
