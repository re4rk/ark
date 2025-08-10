package io.ark.springboot.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: ErrorCode, val message: String, val logLevel: LogLevel) {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),

    // 파일 관련 오류
    FILE_UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "지원하지 않는 파일 형식입니다.", LogLevel.WARN),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "파일 크기가 제한을 초과했습니다.", LogLevel.WARN),
    FILE_MISSING(HttpStatus.BAD_REQUEST, ErrorCode.E400, "업로드할 파일이 없습니다.", LogLevel.WARN),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "파일을 찾을 수 없습니다.", LogLevel.WARN),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, ErrorCode.E403, "파일에 접근할 권한이 없습니다.", LogLevel.WARN),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "파일 업로드 중 오류가 발생했습니다.", LogLevel.ERROR),
    FILE_TOO_MANY_UPLOADS(HttpStatus.BAD_REQUEST, ErrorCode.E400, "동시 업로드 파일 수가 제한을 초과했습니다.", LogLevel.WARN),
}
