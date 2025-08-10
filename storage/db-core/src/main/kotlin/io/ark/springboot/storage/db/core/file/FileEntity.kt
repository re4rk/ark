package io.ark.springboot.storage.db.core.file

import io.ark.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "files")
data class FileEntity(
    @Column(nullable = false, name = "original_name")
    val originalName: String,

    @Column(nullable = false, name = "file_key")
    val key: String,

    @Column(nullable = false, name = "size")
    val size: Long,

    @Column(nullable = false, name = "mime_type")
    val mimeType: String,

    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    val status: UploadStatus,
) : BaseEntity()

enum class UploadStatus {
    PENDING,
    UPLOADED,
    FAILED,
}
