package com.inkvite.inkviteback.storage.controller

import com.inkvite.inkviteback.common.AbstractControllerAdvice
import com.inkvite.inkviteback.storage.exception.ImageTooLargeException
import com.inkvite.inkviteback.storage.exception.ImageUploadFailedException
import com.inkvite.inkviteback.storage.exception.InvalidImageContentTypeException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class StorageControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(InvalidImageContentTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidImageContentType(e: InvalidImageContentTypeException) = handleException(e)

    @ExceptionHandler(ImageTooLargeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleImageTooLarge(e: ImageTooLargeException) = handleException(e)

    @ExceptionHandler(ImageUploadFailedException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleImageUploadFailed(e: ImageUploadFailedException) = handleException(e, is5xx = true)
}
