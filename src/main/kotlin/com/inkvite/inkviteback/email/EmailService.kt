package com.inkvite.inkviteback.email

fun interface EmailService {
    fun sendVerificationEmail(to: String, token: String)
}