package io.ark.springboot.core.domain.file.validator

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ImageFileValidator : FileValidator {

    override fun validate(file: MultipartFile): ValidationResult {
        if (file.isEmpty) {
            return ValidationResult(false, "파일이 비어있습니다.")
        }

        if (file.size > MAX_IMAGE_SIZE) {
            return ValidationResult(false, "이미지 파일 크기는 ${MAX_IMAGE_SIZE / (1024 * 1024)}MB 이하여야 합니다.")
        }

        return ValidationResult(true)
    }

    override fun supports(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    companion object {
        const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10MB
    }
}
