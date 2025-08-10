package io.ark.springboot.core.domain.file

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.client.s3.StorageUploadResult
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Optional

class FileServiceTest {
    private lateinit var fileRepository: FileRepository
    private lateinit var s3Client: S3Client
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileRepository = mockk()
        s3Client = mockk()
        fileService = spyk(FileService(fileRepository, s3Client))
    }

    private fun mockFileEntity(
        id: Long = 1L,
        originalName: String,
        s3Key: String,
        size: Long,
        mimeType: String,
        status: UploadStatus,
        now: LocalDateTime = LocalDateTime.now(),
    ): FileEntity {
        val entity = mockk<FileEntity>()
        every { entity.id } returns id
        every { entity.originalName } returns originalName
        every { entity.s3Key } returns s3Key
        every { entity.size } returns size
        every { entity.mimeType } returns mimeType
        every { entity.status } returns status
        every { entity.createdAt } returns now
        every { entity.updatedAt } returns now
        every {
            entity.copy(
                originalName = any(),
                s3Key = any(),
                size = any(),
                mimeType = any(),
                status = any(),
            )
        } answers {
            mockFileEntity(
                id = id,
                originalName = arg<String>(0),
                s3Key = arg<String>(1),
                size = arg<Long>(2),
                mimeType = arg<String>(3),
                status = arg<UploadStatus>(4),
                now = now,
            )
        }
        every { entity.copy(status = any()) } answers {
            mockFileEntity(
                id = id,
                originalName = originalName,
                s3Key = s3Key,
                size = size,
                mimeType = mimeType,
                status = arg<UploadStatus>(0),
                now = now,
            )
        }
        every { entity.copy(s3Key = any(), status = any()) } answers {
            mockFileEntity(
                id = id,
                originalName = originalName,
                s3Key = arg<String>(0),
                size = size,
                mimeType = mimeType,
                status = arg<UploadStatus>(1),
                now = now,
            )
        }
        return entity
    }

    @Test
    fun `파일 업로드 성공`() {
        // given
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"
        val now = LocalDateTime.now()

        val savedEntity = mockFileEntity(
            originalName = originalName,
            s3Key = "",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            now = now,
        )

        val entitySlot = slot<FileEntity>()
        every { fileRepository.save(capture(entitySlot)) } returns savedEntity
        every { fileService.processFileUpload(any(), any(), any(), any()) } returns Unit

        // when
        val result = fileService.uploadFile(bytes, originalName, contentType)

        // then
        assertThat(result.originalName).isEqualTo(originalName)
        assertThat(result.status).isEqualTo(UploadStatus.PENDING)
        assertThat(result.size).isEqualTo(bytes.size.toLong())
        assertThat(entitySlot.captured.originalName).isEqualTo(originalName)
        assertThat(entitySlot.captured.status).isEqualTo(UploadStatus.PENDING)
    }

    @Test
    fun `지원하지 않는 파일 타입 업로드 시 예외 발생`() {
        // given
        val bytes = "test".toByteArray()
        val originalName = "test.exe"
        val contentType = "application/x-msdownload"

        // when & then
        assertThrows<CoreException> {
            fileService.uploadFile(bytes, originalName, contentType)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
    }

    @Test
    fun `파일 크기 초과 시 예외 발생`() {
        // given
        val bytes = ByteArray(51 * 1024 * 1024) // 51MB
        val originalName = "large.txt"
        val contentType = "text/plain"

        // when & then
        assertThrows<CoreException> {
            fileService.uploadFile(bytes, originalName, contentType)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_SIZE_EXCEEDED)
        }
    }

    @Test
    fun `파일 상태 조회 - PENDING 상태에서 S3에 파일이 있으면 UPLOADED로 변경`() {
        // given
        val fileId = 1L
        val now = LocalDateTime.now()

        val entity = mockFileEntity(
            id = fileId,
            originalName = "test.txt",
            s3Key = "test-key",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.PENDING,
            now = now,
        )

        val updatedEntity = mockFileEntity(
            id = fileId,
            originalName = "test.txt",
            s3Key = "test-key",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            now = now,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { s3Client.exists("test-key") } returns true
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
        val key = "test-key"
        val presignedUrl = "https://example.com/presigned-url"
        every { s3Client.getPresignedUrl(key) } returns presignedUrl

        // when
        val result = fileService.getDownloadUrl(key)

        // then
        assertThat(result).isEqualTo(presignedUrl)
    }

    @Test
    fun `파일 다운로드 URL 조회 실패`() {
        // given
        val key = "test-key"
        every { s3Client.getPresignedUrl(key) } throws RuntimeException("S3 error")

        // when & then
        assertThrows<CoreException> {
            fileService.getDownloadUrl(key)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_NOT_FOUND)
        }
    }

    @Test
    fun `비동기 파일 업로드 성공`() {
        // given
        val fileId = 1L
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"
        val now = LocalDateTime.now()

        val entity = mockFileEntity(
            id = fileId,
            originalName = originalName,
            s3Key = "",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            now = now,
        )

        val uploadResult = StorageUploadResult(
            key = "test-key",
            originalName = originalName,
            size = bytes.size.toLong(),
            mimeType = contentType,
            uploadedAt = now.toString(),
            downloadPath = "/download/test-key",
        )

        val updatedEntity = mockFileEntity(
            id = fileId,
            originalName = originalName,
            s3Key = uploadResult.key,
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.UPLOADED,
            now = now,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { s3Client.upload(bytes, originalName, contentType) } returns uploadResult
        every { fileRepository.save(any()) } returns updatedEntity

        // when
        fileService.processFileUpload(fileId, bytes, originalName, contentType)

        // then
        val entitySlot = slot<FileEntity>()
        verify { fileRepository.save(capture(entitySlot)) }
        assertThat(entitySlot.captured.status).isEqualTo(UploadStatus.UPLOADED)
        assertThat(entitySlot.captured.s3Key).isEqualTo(uploadResult.key)
    }

    @Test
    fun `비동기 파일 업로드 실패`() {
        // given
        val fileId = 1L
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"
        val now = LocalDateTime.now()

        val entity = mockFileEntity(
            id = fileId,
            originalName = originalName,
            s3Key = "",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            now = now,
        )

        val updatedEntity = mockFileEntity(
            id = fileId,
            originalName = originalName,
            s3Key = "",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.FAILED,
            now = now,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { s3Client.upload(bytes, originalName, contentType) } throws RuntimeException("S3 error")
        every { fileRepository.save(any()) } returns updatedEntity

        // when & then
        assertThrows<CoreException> {
            fileService.processFileUpload(fileId, bytes, originalName, contentType)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UPLOAD_ERROR)
        }

        val entitySlot = slot<FileEntity>()
        verify { fileRepository.save(capture(entitySlot)) }
        assertThat(entitySlot.captured.status).isEqualTo(UploadStatus.FAILED)
    }
}
