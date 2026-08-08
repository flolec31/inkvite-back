package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.exception.*
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AppointmentControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(AppointmentNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAppointmentNotFound(e: AppointmentNotFoundException) = handleException(e)

    @ExceptionHandler(AppointmentBelongsToAnotherArtistException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAppointmentBelongsToAnotherArtist(e: AppointmentBelongsToAnotherArtistException) = handleException(e)

    @ExceptionHandler(AppointmentArchiveStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleAppointmentArchiveState(e: AppointmentArchiveStateException) = handleException(e)
}
