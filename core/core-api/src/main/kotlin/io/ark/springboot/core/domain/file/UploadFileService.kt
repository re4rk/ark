package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.ExternalFileStorage
import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.domain.file.validator.FileValidationDispatcher
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.UploadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile

@Service
class UploadFileService(
    private val fileStorage: FileStorage,
    private val externalFileStorage: ExternalFileStorage,
    private val transactionTemplate: TransactionTemplate,
    private val fileValidationDispatcher: FileValidationDispatcher,
    @Qualifier("applicationTaskExecutor") private val applicationScope: CoroutineScope,
) {
    @Transactional
    suspend fun uploadFile(multipartFile: MultipartFile, category: FileCategory, uploaderId: Long): File {
        val validationResult = fileValidationDispatcher.validateFile(multipartFile)
        if (!validationResult.isValid) {
            throw CoreException(
                ErrorType.FILE_UNSUPPORTED_TYPE,
                data =
                validationResult.errorMessage ?: "Unsupported file type",
            )
        }

        val savedFile = fileStorage.save(multipartFile.mapToFileData(category, uploaderId))

        applicationScope.launch {
            try {
                externalFileStorage.upload(
                    bytes = multipartFile.bytes,
                    key = savedFile.key,
                    originalFilename = savedFile.originalName,
                    contentType = savedFile.mimeType,
                )

                transactionTemplate.execute { fileStorage.updateStatus(savedFile.id, UploadStatus.UPLOADED) }
            } catch (e: Exception) {
                transactionTemplate.execute { fileStorage.updateStatus(savedFile.id, UploadStatus.FAILED) }
                throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
            }
        }

        return savedFile
    }

    private fun MultipartFile.mapToFileData(category: FileCategory, uploaderId: Long): FileData {
        return FileData(
            originalName = this.originalFilename ?: "unknown",
            key = externalFileStorage.generateKey(
                uploaderId = uploaderId.toString(),
                category = category.name,
                originalName = this.originalFilename ?: "unknown",
            ),
            size = this.size,
            mimeType = this.contentType ?: "application/octet-stream",
            status = UploadStatus.PENDING,
            category = category,
            uploaderId = uploaderId,
        )
    }
}
