package io.ark.springboot.core.domain.file.validator

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileValidationService(
    private val validators: List<FileValidator>,
) {

    fun validateFile(file: MultipartFile): ValidationResult {
        if (file.isEmpty) {
            return ValidationResult(false, "파일이 비어있습니다.")
        }

        val mimeType = file.contentType ?: "application/octet-stream"

        // 지원하는 validator 찾기
        val validator = validators.find { it.supports(mimeType) }

        if (validator == null) {
            return ValidationResult(false, "지원하지 않는 파일 타입입니다: $mimeType")
        }

        // validator로 검증 수행
        return validator.validate(file)
    }

    fun getSupportedMimeTypes(): Set<String> {
        return validators.flatMap { validator ->
            when (validator) {
                is ImageFileValidator -> listOf("image/*")
                else -> emptyList()
            }
        }.toSet()
    }
}
