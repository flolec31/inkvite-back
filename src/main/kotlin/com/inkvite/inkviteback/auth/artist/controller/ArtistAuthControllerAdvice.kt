package com.inkvite.inkviteback.auth.artist.controller

import com.inkvite.inkviteback.auth.artist.exception.AccountNotActivatedException
import com.inkvite.inkviteback.auth.artist.exception.EmailAlreadyRegisteredException
import com.inkvite.inkviteback.auth.artist.exception.InvalidCredentialsException
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ArtistAuthControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyRegistered(e: EmailAlreadyRegisteredException) = handleException(e)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = handleException(e)

    @ExceptionHandler(AccountNotActivatedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccountNotActivated(e: AccountNotActivatedException) = handleException(e)
}
