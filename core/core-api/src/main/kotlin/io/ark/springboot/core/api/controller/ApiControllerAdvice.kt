package io.ark.springboot.core.api.controller

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.core.support.response.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiControllerAdvice {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(CoreException::class)
    fun handleCoreException(e: CoreException): ResponseEntity<ApiResponse<Any>> {
        when (e.errorType.logLevel) {
            LogLevel.ERROR -> log.error("CoreException : {}", e.message, e)
            LogLevel.WARN -> log.warn("CoreException : {}", e.message, e)
            else -> log.info("CoreException : {}", e.message, e)
        }
        return ResponseEntity(ApiResponse.error(e.errorType, e.data), e.errorType.status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Any>> {
        val cause = e.cause

        when {
            cause is InvalidFormatException && cause.targetType.isEnum -> {
                // Enum 값 오류
                val errorData = mapOf(
                    "invalidValue" to cause.value.toString(),
                    "acceptedValues" to (cause.targetType.enumConstants?.joinToString(", ") ?: ""),
                )
                return handleCoreException(CoreException(ErrorType.INVALID_ENUM_VALUE, errorData))
            }
            else -> {
                // JSON 파싱 오류 등
                val errorData = mapOf(
                    "message" to e.message,
                    "detail" to "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
                )
                return handleCoreException(CoreException(ErrorType.INVALID_REQUEST_FORMAT, errorData))
            }
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Any>> {
        log.error("Exception : {}", e.message, e)
        return ResponseEntity(
            ApiResponse.error(ErrorType.DEFAULT_ERROR),
            ErrorType.DEFAULT_ERROR.status,
        )
    }
}
