package com.inkvite.inkviteback.auth.client.event

data class ClientAccessCodeRequested(
    val to: String,
    val clientFirstName: String,
    val code: String,
)
