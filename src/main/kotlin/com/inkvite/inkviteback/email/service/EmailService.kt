package com.inkvite.inkviteback.email.service

fun interface EmailService {
    fun sendVerificationEmail(to: String, token: String)
}