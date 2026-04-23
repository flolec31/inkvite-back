package com.inkvite.inkviteback.auth.controller

import com.inkvite.inkviteback.auth.exception.*
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import com.inkvite.inkviteback.email.exception.EmailDeliveryException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyRegistered(e: EmailAlreadyRegisteredException) = handleException(e)

    @ExceptionHandler(TokenNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTokenNotFound(e: TokenNotFoundException) = handleException(e)

    @ExceptionHandler(TokenExpiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTokenExpired(e: TokenExpiredException) = handleException(e)

    @ExceptionHandler(EmailDeliveryException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleEmailDelivery(e: EmailDeliveryException) = handleException(e, is5xx = true)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = handleException(e)

    @ExceptionHandler(AccountNotActivatedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccountNotActivated(e: AccountNotActivatedException) = handleException(e)

    @ExceptionHandler(InvalidRefreshTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidRefreshToken(e: InvalidRefreshTokenException) = handleException(e)
}