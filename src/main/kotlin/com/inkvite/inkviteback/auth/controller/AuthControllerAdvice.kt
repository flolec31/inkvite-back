package com.inkvite.inkviteback.auth.controller

import com.inkvite.inkviteback.auth.exception.InvalidRefreshTokenException
import com.inkvite.inkviteback.auth.exception.TokenExpiredException
import com.inkvite.inkviteback.auth.exception.TokenNotFoundException
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Handles the shared `auth.exception` types thrown by both artist and client auth flows
 * (e.g. token verification and refresh-token rotation). Artist- and client-specific
 * exceptions live in their own advices.
 */
@RestControllerAdvice
class AuthControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(TokenNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTokenNotFound(e: TokenNotFoundException) = handleException(e)

    @ExceptionHandler(TokenExpiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTokenExpired(e: TokenExpiredException) = handleException(e)

    @ExceptionHandler(InvalidRefreshTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidRefreshToken(e: InvalidRefreshTokenException) = handleException(e)
}
