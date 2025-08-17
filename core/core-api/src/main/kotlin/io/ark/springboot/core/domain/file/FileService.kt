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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
    private val transactionTemplate: TransactionTemplate,
    private val fileValidationDispatcher: FileValidationDispatcher,
) {
    @Transactional
    suspend fun uploadFile(
        file: MultipartFile,
        category: FileCategory,
        uploaderId: Long,
    ): FileDto {
        if (!fileValidationDispatcher.validateFile(file).isValid) {
            throw CoreException(ErrorType.FILE_UNSUPPORTED_TYPE)
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                fileStorage.upload(
                    bytes = file.bytes,
                    key = key,
                    originalFilename = file.originalFilename ?: "unknown",
                    contentType = file.contentType ?: "application/octet-stream",
                )

                transactionTemplate.execute {
                    val pendedEntity = fileRepository.findById(savedEntity.id)
                        .orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
                    pendedEntity.status = UploadStatus.UPLOADED
                }
            } catch (e: Exception) {
                transactionTemplate.execute {
                    val pendedEntity = fileRepository.findById(savedEntity.id)
                        .orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
                    pendedEntity.status = UploadStatus.FAILED
                }
                throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
            }
        }

        return FileDto.from(savedEntity)
    }

    @Transactional
    fun getFileStatus(id: Long): FileDto {
        val entity = fileRepository.findById(id).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        if (entity.status == UploadStatus.PENDING) {
            if (fileStorage.exists(entity.key)) {
                val updated = entity.copy(status = UploadStatus.UPLOADED)
                return FileDto.from(fileRepository.save(updated))
            }
        }

        return FileDto.from(entity)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(fileId: Long): String {
        val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        when (file.status) {
            UploadStatus.PENDING -> throw CoreException(ErrorType.FILE_PENDING_UPLOAD)
            UploadStatus.FAILED -> throw CoreException(ErrorType.FILE_UPLOAD_ERROR)
            else -> {}
        }

        return try {
            fileStorage.getPresignedUrl(file.key)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_DOWNLOAD_ERROR, cause = e)
        }
    }
}
