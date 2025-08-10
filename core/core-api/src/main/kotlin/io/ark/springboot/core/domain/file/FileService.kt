package io.ark.springboot.core.domain.file

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val s3Client: S3Client,
) {
    @Transactional
    fun uploadFile(
        bytes: ByteArray,
        originalName: String,
        contentType: String,
        category: FileCategory,
        uploaderId: Long,
    ): FileDto {
        if (!validateFileType(contentType)) {
            throw CoreException(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
        if (!validateFileSize(bytes.size.toLong())) {
            throw CoreException(ErrorType.FILE_SIZE_EXCEEDED)
        }

        val key = s3Client.generateKey(uploaderId.toString(), category.name, originalName)
        val entity = FileEntity(
            originalName = originalName,
            key = key,
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
            category = category,
            uploaderId = uploaderId,
        )
        val savedEntity = fileRepository.save(entity)

        processFileUpload(
            fileId = savedEntity.id,
            key = key,
            bytes = bytes,
            originalName = originalName,
            contentType = contentType,
        )

        return FileDto.from(savedEntity)
    }

    @Transactional
    fun getFileStatus(id: Long): FileDto {
        val entity = fileRepository.findById(id).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        if (entity.status == UploadStatus.PENDING) {
            if (s3Client.exists(entity.key)) {
                val updated = entity.copy(status = UploadStatus.UPLOADED)
                return FileDto.from(fileRepository.save(updated))
            }
        }

        return FileDto.from(entity)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(fileId: Long): String {
        try {
            val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

            if (file.status != UploadStatus.UPLOADED) {
                throw CoreException(ErrorType.FILE_NOT_FOUND)
            }

            return s3Client.getPresignedUrl(file.key)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_NOT_FOUND, cause = e)
        }
    }

    // TODO: 파일 타입 검증
    private fun validateFileType(mimeType: String): Boolean {
        return mimeType in ALLOWED_FILE_TYPES
    }

    // TODO: 파일 크기 제한을 설정
    private fun validateFileSize(size: Long): Boolean {
        return size <= MAX_FILE_SIZE
    }

    @Async
    fun processFileUpload(
        fileId: Long,
        key: String,
        bytes: ByteArray,
        originalName: String,
        contentType: String?,
    ) {
        try {
            s3Client.upload(
                bytes = bytes,
                key = key,
                originalFilename = originalName,
                contentType = contentType.toString(),
            )

            val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
            val updated = file.copy(status = UploadStatus.UPLOADED)
            fileRepository.save(updated)
        } catch (e: Exception) {
            val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
            val updated = file.copy(status = UploadStatus.FAILED)
            fileRepository.save(updated)
            try {
                s3Client.delete(file.key)
            } catch (_: Exception) {
            }
            throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
        }
    }

    companion object {
        const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
        val ALLOWED_FILE_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
        )
    }
}
