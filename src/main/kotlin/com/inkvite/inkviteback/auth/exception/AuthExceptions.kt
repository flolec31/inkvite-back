package com.inkvite.inkviteback.auth.exception

class EmailAlreadyRegisteredException : RuntimeException("An account with this email already exists")

class TokenNotFoundException : RuntimeException("Verification token not found or already used")

class TokenExpiredException : RuntimeException("Verification token has expired")
