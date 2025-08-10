package io.ark.springboot.core.domain.file

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.client.s3.StorageUploadResult
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.FileEntity
import io.ark.springboot.storage.db.core.FileRepository
import io.ark.springboot.storage.db.core.UploadStatus
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
        contentType: String?,
    ): FileEntity {
        if (!validateFileType(contentType ?: "")) {
            throw CoreException(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
        if (!validateFileSize(bytes.size.toLong())) {
            throw CoreException(ErrorType.FILE_SIZE_EXCEEDED)
        }

        // S3 업로드
        val result: StorageUploadResult = try {
            s3Client.upload(bytes, originalName, contentType)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
        }
        // 메타데이터 저장 (status=UPLOADED)
        val entity = FileEntity(
            originalName = result.originalName,
            s3Key = result.key,
            size = result.size,
            mimeType = result.mimeType,
            status = UploadStatus.UPLOADED,
        )
        return fileRepository.save(entity)
    }

    fun downloadFile(key: String): ByteArray {
        try {
            // S3에서 파일 다운로드
            return s3Client.download(key)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_NOT_FOUND, cause = e)
        }
    }

    @Transactional
    fun updateStatus(id: Long, status: UploadStatus) {
        val file = fileRepository.findById(id).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
        val updated = file.copy(status = status)
        fileRepository.save(updated)
    }

    // TODO: 파일 타입 검증
    private fun validateFileType(mimeType: String): Boolean {
        val allowed = setOf(
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
        return mimeType in allowed
    }

    // TODO: 파일 크기 제한을 설정
    private fun validateFileSize(size: Long): Boolean {
        val maxSize = 50 * 1024 * 1024 // 50MB
        return size <= maxSize
    }
}
