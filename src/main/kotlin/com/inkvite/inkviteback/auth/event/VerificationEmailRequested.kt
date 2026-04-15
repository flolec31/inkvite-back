package com.inkvite.inkviteback.auth.event

data class VerificationEmailRequested(
    val to: String,
    val token: String,
)