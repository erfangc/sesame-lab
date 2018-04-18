package com.erfangc.sesamelab

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

data class ApiError(val timestamp: LocalDateTime, val message: String, val debug: Any?)

@ControllerAdvice
class ApiAdvice {
    @ExceptionHandler(RuntimeException::class)
    fun handleException(ex: RuntimeException): ResponseEntity<ApiError> {
        ex.printStackTrace()
        return ResponseEntity(
                ApiError(timestamp = LocalDateTime.now(), message = "A server side error has occured", debug = null),
                HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}