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

class UploadFileServiceTest {
    private lateinit var fileRepository: FileRepository
    private lateinit var fileStorage: FileStorage
    private lateinit var transactionTemplate: TransactionTemplate
    private lateinit var fileValidationDispatcher: FileValidationDispatcher
    private lateinit var applicationScope: CoroutineScope
    private lateinit var uploadFileService: UploadFileService

    @BeforeEach
    fun setUp() {
        fileRepository = mockk()
        fileStorage = mockk()
        transactionTemplate = FakeTransactionTemplate()
        fileValidationDispatcher = mockk()
        applicationScope = CoroutineScope(Dispatchers.Unconfined)
        uploadFileService = spyk(UploadFileService(fileRepository, fileStorage, transactionTemplate, fileValidationDispatcher, applicationScope))
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
        val result = uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L)

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
            uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L)
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
            uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L)
        }.also {
            assertThat(it.errorType).isEqualTo(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
    }
}
