package com.stencilia.stenciliaback.artist.controller

import com.stencilia.stenciliaback.artist.exception.SlugAlreadyTakenException
import com.stencilia.stenciliaback.artist.exception.TattooArtistNotFoundException
import com.stencilia.stenciliaback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TattooArtistControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(SlugAlreadyTakenException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleSlugAlreadyTaken(e: SlugAlreadyTakenException) = handleException(e)

    @ExceptionHandler(TattooArtistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTattooArtistNotFound(e: TattooArtistNotFoundException) = handleException(e)
}