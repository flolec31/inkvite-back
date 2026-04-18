package com.inkvite.inkviteback.artist.controller

import com.inkvite.inkviteback.artist.exception.InvalidPhotoContentTypeException
import com.inkvite.inkviteback.artist.exception.SlugAlreadyTakenException
import com.inkvite.inkviteback.artist.exception.TattooArtistNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TattooArtistControllerAdvice {

    @ExceptionHandler(SlugAlreadyTakenException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleSlugAlreadyTaken(e: SlugAlreadyTakenException) =
        mapOf("error" to e.message)

    @ExceptionHandler(InvalidPhotoContentTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidPhotoContentType(e: InvalidPhotoContentTypeException) =
        mapOf("error" to e.message)

    @ExceptionHandler(TattooArtistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTattooArtistNotFound(e: TattooArtistNotFoundException) =
        mapOf("error" to e.message)
}