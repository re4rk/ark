package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
) {

    @Transactional
    fun getFileStatus(id: Long): FileData {
        val entity = fileRepository.findById(id).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        if (entity.status == UploadStatus.PENDING && fileStorage.exists(entity.key)) {
            entity.status = UploadStatus.UPLOADED
        }

        return FileData.from(entity)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(fileId: Long): String {
        val entity = fileRepository.findById(fileId).orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

        return when (entity.status) {
            UploadStatus.PENDING -> {
                if (fileStorage.exists(entity.key)) {
                    getPresignedUrl(entity.key)
                } else {
                    throw CoreException(ErrorType.FILE_PENDING_UPLOAD)
                }
            }

            UploadStatus.FAILED -> throw CoreException(ErrorType.FILE_UPLOAD_ERROR)
            UploadStatus.UPLOADED -> getPresignedUrl(entity.key)
        }
    }

    private fun getPresignedUrl(key: String): String {
        return try {
            fileStorage.getPresignedUrl(key)
        } catch (e: Exception) {
            throw CoreException(ErrorType.FILE_DOWNLOAD_ERROR, cause = e)
        }
    }
}
