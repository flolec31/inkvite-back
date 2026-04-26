package com.inkvite.inkviteback.auth.exception

class EmailAlreadyRegisteredException : RuntimeException("An account with this email already exists")

class TokenNotFoundException : RuntimeException("Token not found or already used")

class TokenExpiredException : RuntimeException("Token has expired")

class InvalidCredentialsException : RuntimeException("Invalid email or password")

class AccountNotActivatedException : RuntimeException("Account is not activated")

class InvalidRefreshTokenException : RuntimeException("Refresh token is invalid or expired")
