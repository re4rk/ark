package io.ark.springboot.core.domain.file

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.client.s3.StorageUploadResult
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.Optional

class FileServiceTest {
    private lateinit var fileRepository: FileRepository
    private lateinit var s3Client: S3Client
    private lateinit var transactionTemplate: TransactionTemplate
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileRepository = mockk()
        s3Client = mockk()
        transactionTemplate = FakeTransactionTemplate()
        fileService = spyk(FileService(fileRepository, s3Client, transactionTemplate))
    }

    @Test
    fun `파일 업로드 성공`() {
        // given
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"
        val now = LocalDateTime.now()
        val key = "test-key"

        val savedEntity = FileEntity(
            originalName = originalName,
            key = key,
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        val entitySlot = slot<FileEntity>()
        every { fileRepository.save(capture(entitySlot)) } returns savedEntity
        every { s3Client.generateKey(any(), any(), any()) } returns key
        every { s3Client.upload(any(), any(), any(), any()) } returns StorageUploadResult(
            key = key,
            originalName = originalName,
            size = bytes.size.toLong(),
            mimeType = contentType,
            uploadedAt = now.toString(),
            downloadPath = "/download/$key",
        )
        // processFileUpload 내부에서 호출되는 findById에 대한 설정 추가
        every { fileRepository.findById(savedEntity.id) } returns Optional.of(savedEntity)
        // processFileUpload 메서드를 스파이하여 동기적으로 실행되도록 함
        every { fileService.processFileUpload(any(), any(), any(), any(), any()) } just runs

        // when
        val result = fileService.uploadFile(bytes, originalName, contentType, FileCategory.IMAGE, 1L)

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
            fileService.uploadFile(bytes, originalName, contentType, FileCategory.IMAGE, 1L)
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
            fileService.uploadFile(bytes, originalName, contentType, FileCategory.IMAGE, 1L)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_SIZE_EXCEEDED)
        }
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
        every { s3Client.getPresignedUrl(key) } returns presignedUrl

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
        every { s3Client.getPresignedUrl(key) } throws RuntimeException("S3 error")

        // when & then
        assertThrows<CoreException> {
            fileService.getDownloadUrl(fileId)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_NOT_FOUND)
        }
    }

    @Test
    fun `비동기 파일 업로드 성공`() {
        // given
        val fileId = 1L
        val key = "test-key"
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"
        val now = LocalDateTime.now()

        val entity = FileEntity(
            originalName = originalName,
            key = "test-key",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        val uploadResult = StorageUploadResult(
            key = "test-key",
            originalName = originalName,
            size = bytes.size.toLong(),
            mimeType = contentType,
            uploadedAt = now.toString(),
            downloadPath = "/download/test-key",
        )

        val updatedEntity = FileEntity(
            originalName = originalName,
            key = uploadResult.key,
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.UPLOADED,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { s3Client.upload(bytes, key, originalName, contentType) } returns uploadResult
        every { fileRepository.save(any()) } returns updatedEntity

        // when
        fileService.processFileUpload(fileId, key, bytes, originalName, contentType)
    }

    @Test
    fun `비동기 파일 업로드 실패`() {
        // given
        val fileId = 1L
        val key = "test-key"
        val bytes = "test".toByteArray()
        val originalName = "test.txt"
        val contentType = "text/plain"

        val entity = FileEntity(
            originalName = originalName,
            key = "",
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        every { fileRepository.findById(fileId) } returns Optional.of(entity)
        every { s3Client.upload(bytes, key, originalName, contentType) } throws RuntimeException("S3 error")

        // when & then
        assertThrows<CoreException> {
            fileService.processFileUpload(fileId, key, bytes, originalName, contentType)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UPLOAD_ERROR)
        }
    }
}
