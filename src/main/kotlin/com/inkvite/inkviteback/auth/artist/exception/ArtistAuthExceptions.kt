package com.inkvite.inkviteback.auth.artist.exception

class EmailAlreadyRegisteredException : RuntimeException("An account with this email already exists")

class InvalidCredentialsException : RuntimeException("Invalid email or password")

class AccountNotActivatedException : RuntimeException("Account is not activated")
