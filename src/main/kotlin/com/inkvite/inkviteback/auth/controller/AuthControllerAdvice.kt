package com.inkvite.inkviteback.auth.controller

import com.inkvite.inkviteback.auth.exception.EmailAlreadyRegisteredException
import com.inkvite.inkviteback.auth.exception.TokenExpiredException
import com.inkvite.inkviteback.auth.exception.TokenNotFoundException
import com.inkvite.inkviteback.email.exception.EmailDeliveryException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthControllerAdvice {

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyRegistered(e: EmailAlreadyRegisteredException) =
        mapOf("error" to e.message)

    @ExceptionHandler(TokenNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTokenNotFound(e: TokenNotFoundException) =
        mapOf("error" to e.message)

    @ExceptionHandler(TokenExpiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTokenExpired(e: TokenExpiredException) =
        mapOf("error" to e.message)

    @ExceptionHandler(EmailDeliveryException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleEmailDelivery(e: EmailDeliveryException) =
        mapOf("error" to e.message)
}