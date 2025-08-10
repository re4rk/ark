package io.ark.springboot.core.domain.file

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.FileEntity
import io.ark.springboot.storage.db.core.FileRepository
import io.ark.springboot.storage.db.core.UploadStatus
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
    ): FileDto {
        if (!validateFileType(contentType)) {
            throw CoreException(ErrorType.FILE_UNSUPPORTED_TYPE)
        }
        if (!validateFileSize(bytes.size.toLong())) {
            throw CoreException(ErrorType.FILE_SIZE_EXCEEDED)
        }

        // 메타데이터를 PENDING 상태로 먼저 저장
        val entity = FileEntity(
            originalName = originalName,
            s3Key = "", // S3 업로드 후 업데이트됨
            size = bytes.size.toLong(),
            mimeType = contentType,
            status = UploadStatus.PENDING,
        )
        val savedEntity = fileRepository.save(entity)

        // 비동기로 S3 업로드 실행
        processFileUpload(savedEntity.id, bytes, originalName, contentType)

        return FileDto.from(savedEntity)
    }

    @Transactional
    fun getFileStatus(id: Long): FileDto {
        val entity = fileRepository.findById(id).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        if (entity.status == UploadStatus.PENDING) {
            if (s3Client.exists(entity.s3Key)) {
                val updated = entity.copy(status = UploadStatus.UPLOADED)
                return FileDto.from(fileRepository.save(updated))
            }
        }

        return FileDto.from(entity)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(key: String): String {
        try {
            // S3에서 미리 서명된 URL 생성
            return s3Client.getPresignedUrl(key)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_NOT_FOUND, cause = e)
        }
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

    @Async
    fun processFileUpload(
        fileId: Long,
        bytes: ByteArray,
        originalName: String,
        contentType: String?,
    ) {
        try {
            val result = s3Client.upload(bytes, originalName, contentType)

            val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
            val updated = file.copy(s3Key = result.key, status = UploadStatus.UPLOADED)
            fileRepository.save(updated)
        } catch (e: Exception) {
            val file = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }
            val updated = file.copy(status = UploadStatus.FAILED)
            fileRepository.save(updated)

            throw CoreException(ErrorType.FILE_UPLOAD_ERROR, cause = e)
        }
    }
}
