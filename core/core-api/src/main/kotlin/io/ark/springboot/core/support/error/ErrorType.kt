package io.ark.springboot.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: ErrorCode, val message: String, val logLevel: LogLevel) {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),

    // 파일 관련 오류
    FILE_UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, ErrorCode.FILE_UNSUPPORTED_TYPE, "지원하지 않는 파일 형식입니다.", LogLevel.WARN),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, ErrorCode.FILE_SIZE_EXCEEDED, "파일 크기가 제한을 초과했습니다.", LogLevel.WARN),
    FILE_MISSING(HttpStatus.BAD_REQUEST, ErrorCode.FILE_MISSING, "업로드할 파일이 없습니다.", LogLevel.WARN),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다.", LogLevel.WARN),
    FILE_PENDING_UPLOAD(HttpStatus.BAD_REQUEST, ErrorCode.FILE_PENDING_UPLOAD, "파일이 아직 업로드되지 않았습니다.", LogLevel.WARN),
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_DOWNLOAD_ERROR, "파일 다운로드 중 오류가 발생했습니다.", LogLevel.ERROR),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, ErrorCode.FILE_ACCESS_DENIED, "파일에 접근할 권한이 없습니다.", LogLevel.WARN),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FILE_UPLOAD_ERROR, "파일 업로드 중 오류가 발생했습니다.", LogLevel.ERROR),
    FILE_TOO_MANY_UPLOADS(HttpStatus.BAD_REQUEST, ErrorCode.FILE_TOO_MANY_UPLOADS, "동시 업로드 파일 수가 제한을 초과했습니다.", LogLevel.WARN),

    // 콘텐츠 검증 오류
    CONTENT_INVALID_LENGTH(HttpStatus.BAD_REQUEST, ErrorCode.CONTENT_INVALID_LENGTH, "콘텐츠 길이가 유효하지 않습니다.", LogLevel.WARN),
    CONTENT_BANNED_WORD(HttpStatus.BAD_REQUEST, ErrorCode.CONTENT_BANNED_WORD, "금지어가 포함되어 있습니다.", LogLevel.WARN),
}
