package io.ark.springboot.core.domain.file

import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.UploadStatus

data class FileData(
    val originalName: String,
    val key: String,
    val size: Long,
    val mimeType: String,
    val status: UploadStatus,
    val category: FileCategory,
    val uploaderId: Long,
)
