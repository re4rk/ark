package io.ark.springboot.core.domain.file

import io.ark.springboot.storage.db.core.FileEntity
import io.ark.springboot.storage.db.core.UploadStatus
import java.time.LocalDateTime

data class FileDto(
    val id: Long,
    val originalName: String,
    val s3Key: String,
    val size: Long,
    val mimeType: String,
    val status: UploadStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(entity: FileEntity) = FileDto(
            id = entity.id,
            originalName = entity.originalName,
            s3Key = entity.s3Key,
            size = entity.size,
            mimeType = entity.mimeType,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
