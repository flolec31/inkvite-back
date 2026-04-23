package com.inkvite.inkviteback.artist.controller

import com.inkvite.inkviteback.artist.exception.InvalidPhotoContentTypeException
import com.inkvite.inkviteback.artist.exception.SlugAlreadyTakenException
import com.inkvite.inkviteback.artist.exception.TattooArtistNotFoundException
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TattooArtistControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(SlugAlreadyTakenException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleSlugAlreadyTaken(e: SlugAlreadyTakenException) = handleException(e)

    @ExceptionHandler(InvalidPhotoContentTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidPhotoContentType(e: InvalidPhotoContentTypeException) = handleException(e)

    @ExceptionHandler(TattooArtistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTattooArtistNotFound(e: TattooArtistNotFoundException) = handleException(e)
}