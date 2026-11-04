package com.stencilia.stenciliaback.auth.client.controller

import com.stencilia.stenciliaback.auth.client.exception.InvalidCodeException
import com.stencilia.stenciliaback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ClientAuthControllerAdvice : AbstractControllerAdvice() {

    // InvalidRefreshTokenException (shared, thrown by RefreshTokenService for both audiences)
    // is mapped to 401 globally by AuthControllerAdvice — one handler per exception type.
    @ExceptionHandler(InvalidCodeException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCode(e: InvalidCodeException) = handleException(e)
}
