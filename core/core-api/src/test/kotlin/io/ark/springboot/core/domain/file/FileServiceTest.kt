package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.ExternalFileStorage
import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class FileServiceTest {
    private lateinit var fileStorage: FileStorage
    private lateinit var externalFileStorage: ExternalFileStorage
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileStorage = mockk()
        externalFileStorage = mockk()
        fileService = spyk(FileService(fileStorage, externalFileStorage))
    }

    @Test
    fun `파일 상태 조회 - PENDING 상태에서 S3에 파일이 있으면 UPLOADED로 변경`() {
        // given
        val fileId = 1L

        val file = createFile(id = fileId, status = UploadStatus.PENDING)

        val updatedFile = file.copy(status = UploadStatus.UPLOADED)

        every { fileStorage.getFile(fileId) } returns file
        every { externalFileStorage.exists("test-key") } returns true
        every { fileStorage.updateStatus(fileId, UploadStatus.UPLOADED) } returns updatedFile

        // when
        val result = fileService.findById(fileId)

        // then
        assertThat(result.status).isEqualTo(UploadStatus.UPLOADED)
    }

    @Test
    fun `파일 상태 조회 - 존재하지 않는 파일`() {
        // given
        val fileId = 1L
        every { fileStorage.getFile(fileId) } throws CoreException(ErrorType.FILE_NOT_FOUND)

        // when
        val result = assertThrows<CoreException> { fileService.findById(fileId) }

        // then
        assertThat(result.errorType).isEqualTo(ErrorType.FILE_NOT_FOUND)
    }

    @Test
    fun `파일 다운로드 URL 조회 성공`() {
        // given
        val fileId = 1L
        val key = "test-key"
        val presignedUrl = "https://example.com/presigned-url"

        val file = createFile(id = fileId, key = key, status = UploadStatus.UPLOADED)

        every { fileStorage.getFile(fileId) } returns file
        every { externalFileStorage.getPresignedUrl(key) } returns presignedUrl

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

        val file = createFile(id = fileId, key = key, status = UploadStatus.UPLOADED)

        every { fileStorage.getFile(fileId) } returns file
        every { externalFileStorage.getPresignedUrl(key) } throws RuntimeException("S3 error")

        // when
        val result = assertThrows<CoreException> { fileService.getDownloadUrl(fileId) }

        // then
        assertThat(result.errorType).isEqualTo(ErrorType.FILE_DOWNLOAD_ERROR)
    }

    private fun createFile(
        id: Long = 1L,
        originalName: String = "test.txt",
        key: String = "test-key",
        size: Long = 100L,
        mimeType: String = "text/plain",
        status: UploadStatus = UploadStatus.PENDING,
        category: FileCategory = FileCategory.IMAGE,
        uploaderId: Long = 1L,
    ): File {
        return File(
            id = id,
            originalName = originalName,
            key = key,
            size = size,
            mimeType = mimeType,
            status = status,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            category = category,
            uploaderId = uploaderId,
        )
    }
}
