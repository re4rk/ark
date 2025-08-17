package io.ark.springboot.core.domain.file.validator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class FileValidationServiceTest {

    private lateinit var imageValidator: ImageFileValidator
    private lateinit var fileValidationDispatcher: FileValidationDispatcher

    @BeforeEach
    fun setUp() {
        imageValidator = ImageFileValidator()
        fileValidationDispatcher = FileValidationDispatcher(listOf(imageValidator))
    }

    @Test
    fun `이미지 파일 검증 성공`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".toByteArray(),
        )

        // when
        val result = fileValidationDispatcher.validateFile(file)

        // then
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `지원하지 않는 파일 타입 검증 실패`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "test exe content".toByteArray(),
        )

        // when
        val result = fileValidationDispatcher.validateFile(file)

        // then
        assertThat(result.isValid).isFalse()
        assertThat(result.errorMessage).contains("지원하지 않는 파일 타입입니다")
    }

    @Test
    fun `빈 파일 검증 실패`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            ByteArray(0),
        )

        // when
        val result = fileValidationDispatcher.validateFile(file)

        // then
        assertThat(result.isValid).isFalse()
        assertThat(result.errorMessage).contains("파일이 비어있습니다")
    }

    @Test
    fun `이미지 파일 크기 초과 검증 실패`() {
        // given
        val largeBytes = ByteArray(ImageFileValidator.MAX_IMAGE_SIZE + 1)
        val file = MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeBytes,
        )

        // when
        val result = fileValidationDispatcher.validateFile(file)

        // then
        assertThat(result.isValid).isFalse()
        assertThat(result.errorMessage).contains("이미지 파일 크기는 10MB 이하여야 합니다")
    }

    @Test
    fun `지원하는 MIME 타입 목록 조회`() {
        // when
        val supportedTypes = fileValidationDispatcher.getSupportedMimeTypes()

        // then
        assertThat(supportedTypes).contains("image/*")
    }
}
