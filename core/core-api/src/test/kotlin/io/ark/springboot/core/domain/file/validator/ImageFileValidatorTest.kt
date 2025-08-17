package io.ark.springboot.core.domain.file.validator

import io.ark.springboot.core.domain.file.validator.ImageFileValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class ImageFileValidatorTest {

    private val validator = ImageFileValidator()

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
        val result = validator.validate(file)

        // then
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `PNG 이미지 파일 검증 성공`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "test png content".toByteArray(),
        )

        // when
        val result = validator.validate(file)

        // then
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `GIF 이미지 파일 검증 성공`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.gif",
            "image/gif",
            "test gif content".toByteArray(),
        )

        // when
        val result = validator.validate(file)

        // then
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `빈 이미지 파일 검증 실패`() {
        // given
        val file = MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            ByteArray(0),
        )

        // when
        val result = validator.validate(file)

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
        val result = validator.validate(file)

        // then
        assertThat(result.isValid).isFalse()
        assertThat(result.errorMessage).contains("이미지 파일 크기는 10MB 이하여야 합니다")
    }

    @Test
    fun `이미지 MIME 타입 지원 확인`() {
        // when & then
        assertThat(validator.supports("image/jpeg")).isTrue()
        assertThat(validator.supports("image/png")).isTrue()
        assertThat(validator.supports("image/gif")).isTrue()
        assertThat(validator.supports("image/webp")).isTrue()
        assertThat(validator.supports("image/bmp")).isTrue()
    }

    @Test
    fun `이미지가 아닌 MIME 타입 지원하지 않음`() {
        // when & then
        assertThat(validator.supports("application/pdf")).isFalse()
        assertThat(validator.supports("text/plain")).isFalse()
        assertThat(validator.supports("application/zip")).isFalse()
    }
}
