package io.ark.springboot.core.domain.file.storage

import io.ark.springboot.core.domain.file.FileData
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.file.FileCategory
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
    fun save(
        originalName: String,
        key: String,
        size: Long,
        mimeType: String,
        status: UploadStatus,
        category: FileCategory,
        uploaderId: Long,
    ): FileData {
        val entity = FileEntity(
            originalName = originalName,
            key = key,
            size = size,
            mimeType = mimeType,
            status = status,
            category = category,
            uploaderId = uploaderId,
        )
        val savedEntity = fileRepository.save(entity)
        return FileData.from(savedEntity)
    }

    fun getFile(id: Long): FileData = fileRepository.findById(id)
        .map { FileData.from(it) }
        .orElseThrow { CoreException(ErrorType.FILE_NOT_FOUND) }

    @Transactional
    fun updateStatus(id: Long, status: UploadStatus): FileData {
        val entity = fileRepository.findByIdOrNull(id)
            ?: throw CoreException(ErrorType.FILE_NOT_FOUND)
        entity.status = status
        val savedEntity = fileRepository.save(entity)
        return FileData.from(savedEntity)
    }
}
