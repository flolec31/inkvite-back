package com.stencilia.stenciliaback.support.event

data class SupportMessageConfirmationEmailRequested(
    val to: String,
    val artistName: String,
)
