package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class FileServiceTest {
    private lateinit var fileRepository: FileRepository
    private lateinit var fileStorage: FileStorage
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileRepository = mockk()
        fileStorage = mockk()
        fileService = spyk(FileService(fileRepository, fileStorage))
    }

    @Test
    fun `파일 상태 조회 - PENDING 상태에서 S3에 파일이 있으면 UPLOADED로 변경`() {
        // given
        val fileId = 1L

        val entity = FileEntity(
            originalName = "test.txt",
            key = "test-key",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.PENDING,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        val updatedEntity = FileEntity(
            originalName = "test.txt",
            key = "test-key",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { fileStorage.exists("test-key") } returns true
        every { fileRepository.save(any()) } returns updatedEntity

        // when
        val result = fileService.getFileStatus(fileId)

        // then
        assertThat(result.status).isEqualTo(UploadStatus.UPLOADED)
    }

    @Test
    fun `파일 상태 조회 - 존재하지 않는 파일`() {
        // given
        val fileId = 1L
        every { fileRepository.findById(fileId) } returns Optional.empty()

        // when & then
        assertThrows<CoreException> {
            fileService.getFileStatus(fileId)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_NOT_FOUND)
        }
    }

    @Test
    fun `파일 다운로드 URL 조회 성공`() {
        // given
        val fileId = 1L
        val key = "test-key"
        val presignedUrl = "https://example.com/presigned-url"

        val entity = FileEntity(
            originalName = "test.txt",
            key = key,
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { fileStorage.getPresignedUrl(key) } returns presignedUrl

        // when
        val result = fileService.getDownloadUrl(fileId)

        // then
        assertThat(result).isEqualTo(presignedUrl)
    }

    @Test
    fun `파일 다운로드 URL 조회 실패`() {
        // given
        val fileId = 1L
        val key = "test-key"

        val entity = FileEntity(
            originalName = "test.txt",
            key = key,
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { fileStorage.getPresignedUrl(key) } throws RuntimeException("S3 error")

        // when & then
        assertThrows<CoreException> {
            fileService.getDownloadUrl(fileId)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_DOWNLOAD_ERROR)
        }
    }
}
