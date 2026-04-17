package com.inkvite.inkviteback.auth.service

interface AuthService {
    fun register(email: String, password: String)
    fun verify(token: String)
    fun resendVerification(email: String)
}