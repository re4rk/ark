package io.ark.springboot.core.domain.file

import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.FileEntity
import io.ark.springboot.storage.db.core.file.UploadStatus
import java.time.LocalDateTime

data class File(
    val id: Long,
    val originalName: String,
    val key: String,
    val size: Long,
    val mimeType: String,
    val status: UploadStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val category: FileCategory,
    val uploaderId: Long,
) {
    companion object {
        fun from(entity: FileEntity) = File(
            id = entity.id,
            originalName = entity.originalName,
            key = entity.key,
            size = entity.size,
            mimeType = entity.mimeType,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            category = entity.category,
            uploaderId = entity.uploaderId,
        )
    }
}
