package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.exception.AppointmentFormNotFoundException
import com.inkvite.inkviteback.appointment.exception.InvalidReferenceContentTypeException
import com.inkvite.inkviteback.appointment.exception.ReferenceUploadFailedException
import com.inkvite.inkviteback.appointment.exception.ReferenceTooLargeException
import com.inkvite.inkviteback.common.AbstractControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AppointmentControllerAdvice : AbstractControllerAdvice() {

    @ExceptionHandler(AppointmentFormNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAppointmentFormNotFound(e: AppointmentFormNotFoundException) = handleException(e)

    @ExceptionHandler(InvalidReferenceContentTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidReferenceContentType(e: InvalidReferenceContentTypeException) = handleException(e)

    @ExceptionHandler(ReferenceTooLargeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleReferenceTooLarge(e: ReferenceTooLargeException) = handleException(e)

    @ExceptionHandler(ReferenceUploadFailedException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleReferenceUploadFailed(e: ReferenceUploadFailedException) = handleException(e, is5xx = true)
}