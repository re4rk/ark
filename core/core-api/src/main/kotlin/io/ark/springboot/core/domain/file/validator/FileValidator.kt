package io.ark.springboot.core.domain.file.validator

import org.springframework.web.multipart.MultipartFile

interface FileValidator {
    fun validate(file: MultipartFile): ValidationResult
    fun supports(mimeType: String): Boolean
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
)
