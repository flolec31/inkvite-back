package com.stencilia.stenciliaback.auth.artist.controller

import com.stencilia.stenciliaback.auth.artist.exception.AccountNotActivatedException
import com.stencilia.stenciliaback.auth.artist.exception.EmailAlreadyRegisteredException
import com.stencilia.stenciliaback.auth.artist.exception.InvalidCredentialsException
import com.stencilia.stenciliaback.common.AbstractControllerAdvice
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
