package com.inkvite.inkviteback.support.event

data class SupportMessageConfirmationEmailRequested(
    val to: String,
    val artistName: String,
)
