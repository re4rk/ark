package io.ark.springboot.core.domain.file

import io.ark.springboot.core.domain.file.storage.ExternalFileStorage
import io.ark.springboot.core.domain.file.storage.FileStorage
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.UploadStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FileService(
    private val fileStorage: FileStorage,
    private val externalFileStorage: ExternalFileStorage,
) {

    @Transactional
    fun getFileStatus(id: Long): File {
        val file = fileStorage.getFile(id)

        if (file.status == UploadStatus.PENDING && externalFileStorage.exists(file.key)) {
            return fileStorage.updateStatus(id, UploadStatus.UPLOADED)
        }

        return file
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(fileId: Long): String {
        val file = fileStorage.getFile(fileId)

        return when (file.status) {
            UploadStatus.PENDING -> if (externalFileStorage.exists(file.key)) {
                externalFileStorage.getPresignedUrl(file.key)
            } else {
                throw CoreException(ErrorType.FILE_PENDING_UPLOAD)
            }

            UploadStatus.FAILED -> throw CoreException(ErrorType.FILE_UPLOAD_ERROR)
            UploadStatus.UPLOADED -> try {
                externalFileStorage.getPresignedUrl(file.key)
            } catch (e: Exception) {
                throw CoreException(ErrorType.FILE_DOWNLOAD_ERROR, cause = e)
            }
        }
    }
}
