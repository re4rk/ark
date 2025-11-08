package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.domain.file.validator.FileValidationDispatcher
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile

@Service
class UploadFileService(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
    private val transactionTemplate: TransactionTemplate,
    private val fileValidationDispatcher: FileValidationDispatcher,
    @Qualifier("applicationTaskExecutor") private val applicationScope: CoroutineScope,
) {
    @Transactional
    suspend fun uploadFile(
        file: MultipartFile,
        category: FileCategory,
        uploaderId: Long,
    ): FileDto {
        val validationResult = fileValidationDispatcher.validateFile(file)
        if (!validationResult.isValid) {
            throw CoreException(
                ErrorType.FILE_UNSUPPORTED_TYPE,
                data = validationResult.errorMessage ?: "Unsupported file type",
            )
        }

        val key = fileStorage.generateKey(uploaderId.toString(), category.name, file.originalFilename ?: "unknown")
        val entity = FileEntity(
            originalName = file.originalFilename ?: "unknown",
            key = key,
            size = file.size,
            mimeType = file.contentType ?: "application/octet-stream",
            status = UploadStatus.PENDING,
            category = category,
            uploaderId = uploaderId,
        )

        val savedEntity = fileRepository.save(entity)

        applicationScope.launch {
            try {
                fileStorage.upload(
                    bytes = file.bytes,
                    key = key,
                    originalFilename = file.originalFilename ?: "unknown",
                    contentType = file.contentType ?: "application/octet-stream",
                )

                transactionTemplate.execute {
                    val pendedEntity = fileRepository.findByIdOrNull(savedEntity.id)
                        ?: throw CoreException(ErrorType.FILE_NOT_FOUND)
                    pendedEntity.status = UploadStatus.UPLOADED
                }
            } catch (e: Exception) {
                transactionTemplate.execute {
                    val pendedEntity = fileRepository.findByIdOrNull(savedEntity.id)
                        ?: throw CoreException(ErrorType.FILE_NOT_FOUND)
                    pendedEntity.status = UploadStatus.FAILED
                }
                throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
            }
        }

        return FileDto.from(savedEntity)
    }
}
