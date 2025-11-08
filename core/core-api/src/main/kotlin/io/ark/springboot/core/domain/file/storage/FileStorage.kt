package io.ark.springboot.core.domain.file.storage

import io.ark.springboot.core.domain.file.File
import io.ark.springboot.core.domain.file.FileData
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.FileRepository
import io.ark.springboot.storage.db.core.file.UploadStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class FileStorage(
    private val fileRepository: FileRepository,
) {
    @Transactional
    fun save(fileData: FileData): File {
        val entity = fileData.toFileEntity()
        return fileRepository.save(entity).toFile()
    }

    fun getFile(id: Long): File = fileRepository.findById(id)
        .map { it.toFile() }
        .orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

    @Transactional
    fun updateStatus(id: Long, status: UploadStatus): File {
        val entity = fileRepository.findByIdOrNull(id) ?: throw CoreException(ErrorType.FILE_NOT_FOUND)
        entity.status = status
        return entity.toFile()
    }

    companion object {
        private fun FileEntity.toFile() = File(
            id = id,
            originalName = originalName,
            key = key,
            size = size,
            mimeType = mimeType,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            category = category,
            uploaderId = uploaderId,
        )

        private fun FileData.toFileEntity() = FileEntity(
            originalName = originalName,
            key = key,
            size = size,
            mimeType = mimeType,
            status = status,
            category = category,
            uploaderId = uploaderId,
        )
    }
}
