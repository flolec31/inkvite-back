package com.inkvite.inkviteback.discussion.controller

import com.inkvite.inkviteback.common.AbstractControllerAdvice
import com.inkvite.inkviteback.discussion.exception.InvalidMessageImageKeyException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DiscussionControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(InvalidMessageImageKeyException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidMessageImageKey(e: InvalidMessageImageKeyException) = handleException(e)
}
