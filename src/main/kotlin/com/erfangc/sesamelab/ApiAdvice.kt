package com.erfangc.sesamelab

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.sql.SQLException
import java.time.LocalDateTime

data class ApiError(val timestamp: LocalDateTime = LocalDateTime.now(), val message: String, val debug: Any?)

@ControllerAdvice
class ApiAdvice {
    @ExceptionHandler(RuntimeException::class)
    fun handleException(ex: RuntimeException): ResponseEntity<ApiError> {
        ex.printStackTrace()
        return ResponseEntity(
                ApiError(message = "A server side error has occured", debug = null),
                HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(ex: SQLException): ResponseEntity<ApiError> {
        ex.printStackTrace()
        return ResponseEntity(
                ApiError(message = "A database error has occured", debug = ex.sqlState),
                HttpStatus.BAD_REQUEST
        )
    }
}