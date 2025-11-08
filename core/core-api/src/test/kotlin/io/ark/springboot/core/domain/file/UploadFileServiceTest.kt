package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.ExternalFileStorage
import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.domain.file.validator.FileValidationDispatcher
import io.ark.springboot.core.domain.file.validator.ValidationResult
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    private lateinit var fileStorage: FileStorage
    private lateinit var externalFileStorage: ExternalFileStorage
    private lateinit var transactionTemplate: TransactionTemplate
    private lateinit var fileValidationDispatcher: FileValidationDispatcher
    private lateinit var applicationScope: CoroutineScope
    private lateinit var uploadFileService: UploadFileService

    @BeforeEach
    fun setUp() {
        fileStorage = mockk()
        externalFileStorage = mockk()
        transactionTemplate = FakeTransactionTemplate()
        fileValidationDispatcher = mockk()
        applicationScope = CoroutineScope(Dispatchers.Unconfined)
        uploadFileService =
            spyk(UploadFileService(fileStorage, externalFileStorage, transactionTemplate, fileValidationDispatcher, applicationScope))
    }

    @Test
    fun `파일 업로드 성공`() = runTest {
        // given
        val file = createMockMultipartFile()
        val key = "test-key"

        val savedFile = createFile(
            id = 1L,
            originalName = file.originalFilename,
            key = key,
            size = file.size,
            mimeType = file.contentType ?: "application/octet-stream",
            status = UploadStatus.PENDING,
        )

        val updatedFile = savedFile.copy(status = UploadStatus.UPLOADED)

        every { externalFileStorage.generateKey(any(), any(), any()) } returns key
        every { fileStorage.save(any()) } returns savedFile
        every { fileStorage.updateStatus(any(), UploadStatus.UPLOADED) } returns updatedFile
        coEvery { externalFileStorage.upload(any(), any(), any(), any()) } just runs
        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(true)

        // when
        val result = uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L)

        // then
        assertThat(result.originalName).isEqualTo(file.originalFilename)
        assertThat(result.status).isEqualTo(UploadStatus.PENDING)
        assertThat(result.size).isEqualTo(file.size)
    }

    @Test
    fun `지원하지 않는 파일 타입 업로드 시 예외 발생`() = runTest {
        // given
        val file = createMockMultipartFile(originalFilename = "test.exe", contentType = "application/x-msdownload")

        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(false, "지원하지 않는 파일 타입입니다")

        // when
        val result = assertThrows<CoreException> { uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L) }

        // then
        assertThat(result.errorType).isEqualTo(ErrorType.FILE_UNSUPPORTED_TYPE)
    }

    @Test
    fun `파일 크기 초과 시 예외 발생`() = runTest {
        // given
        val file = createMockMultipartFile(originalFilename = "large.txt", content = ByteArray(51 * 1024 * 1024))

        every { fileValidationDispatcher.validateFile(any()) } returns ValidationResult(false, "파일 크기가 제한을 초과했습니다")

        // when
        val result = assertThrows<CoreException> { uploadFileService.uploadFile(file, FileCategory.IMAGE, 1L) }

        // then
        assertThat(result.errorType).isEqualTo(ErrorType.FILE_SIZE_EXCEEDED)
    }

    private fun createMockMultipartFile(
        name: String = "file",
        originalFilename: String = "test.txt",
        contentType: String = "text/plain",
        content: ByteArray = "test".toByteArray(),
    ): MockMultipartFile {
        return MockMultipartFile(
            name,
            originalFilename,
            contentType,
            content,
        )
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
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now(),
            category = category,
            uploaderId = uploaderId,
        )
    }
}
