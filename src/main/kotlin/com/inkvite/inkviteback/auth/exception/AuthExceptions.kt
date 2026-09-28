package com.inkvite.inkviteback.auth.exception

class TokenNotFoundException : RuntimeException("Token not found or already used")

class TokenExpiredException : RuntimeException("Token has expired")

class InvalidRefreshTokenException : RuntimeException("Refresh token is invalid or expired")
