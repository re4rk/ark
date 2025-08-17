package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.domain.file.validator.FileValidationDispatcher
import io.ark.springboot.core.domain.file.validator.ValidationResult
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.support.TransactionTemplate
import java.util.Optional

class FileServiceTest {
    private lateinit var fileRepository: FileRepository
    private lateinit var fileStorage: FileStorage
    private lateinit var transactionTemplate: TransactionTemplate
    private lateinit var fileValidationDispatcher: FileValidationDispatcher
    private lateinit var applicationScope: CoroutineScope
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileRepository = mockk()
        fileStorage = mockk()
        transactionTemplate = FakeTransactionTemplate()
        fileValidationDispatcher = mockk()
        applicationScope = CoroutineScope(Dispatchers.Unconfined)
        fileService = spyk(FileService(fileRepository, fileStorage, transactionTemplate, fileValidationDispatcher, applicationScope))
    }

    @Test
    fun `파일 업로드 성공`() = runTest {
        // given
        val file = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test".toByteArray(),
        )
        val key = "test-key"

        val savedEntity = FileEntity(
            originalName = file.originalFilename,
            key = key,
            size = file.size,
            mimeType = file.contentType ?: "application/octet-stream",
            status = UploadStatus.PENDING,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )

        val updatedEntity = savedEntity.copy(status = UploadStatus.UPLOADED)
        val entitySlot = slot<FileEntity>()
        every { fileRepository.save(capture(entitySlot)) } returns savedEntity andThen updatedEntity
        every { fileStorage.generateKey(any(), any(), any()) } returns key
        coEvery { fileStorage.upload(any(), any(), any(), any()) } just runs
        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(true)

        // when
        val result = fileService.uploadFile(file, FileCategory.IMAGE, 1L)

        // then
        assertThat(result.originalName).isEqualTo(file.originalFilename)
        assertThat(result.status).isEqualTo(UploadStatus.PENDING)
        assertThat(result.size).isEqualTo(file.size)
        assertThat(entitySlot.captured.originalName).isEqualTo(file.originalFilename)
        assertThat(entitySlot.captured.size).isEqualTo(file.size)
        assertThat(entitySlot.captured.mimeType).isEqualTo(file.contentType)
    }

    @Test
    fun `지원하지 않는 파일 타입 업로드 시 예외 발생`() = runTest {
        // given
        val file = MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "test".toByteArray(),
        )

        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(false, "지원하지 않는 파일 타입입니다")

        // when & then
        assertThrows<CoreException> {
            fileService.uploadFile(file, FileCategory.IMAGE, 1L)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
    }

    @Test
    fun `파일 크기 초과 시 예외 발생`() = runTest {
        // given
        val file = MockMultipartFile(
            "file",
            "large.txt",
            "text/plain",
            ByteArray(51 * 1024 * 1024), // 51MB
        )

        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(false, "파일 크기가 제한을 초과했습니다")

        // when & then
        assertThrows<CoreException> {
            fileService.uploadFile(file, FileCategory.IMAGE, 1L)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UNSUPPORTED_TYPE)
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
