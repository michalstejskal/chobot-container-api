package cz.chobot.container_api.controller

import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.exception.ErrorDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.sql.Timestamp

/***
 * Handle all execptions in application and return to caller only generic info that something went wrong.
 */
@ControllerAdvice
@RestController
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ControllerException::class)
    fun handleUserNotFoundException(ex: ControllerException): ResponseEntity<ErrorDetail> {
        val errorDetails = ErrorDetail(Timestamp(0), ex.message!!)
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }
}