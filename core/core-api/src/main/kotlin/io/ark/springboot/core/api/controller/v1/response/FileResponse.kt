package io.ark.springboot.core.api.controller.v1.response

import io.ark.springboot.core.domain.file.FileData
import io.ark.springboot.storage.db.core.file.UploadStatus

data class FileResponse(
    val id: Long,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val status: UploadStatus,
    val url: String?,
) {
    companion object {
        fun from(dto: FileData, url: String?) = FileResponse(
            id = dto.id,
            originalName = dto.originalName,
            size = dto.size,
            mimeType = dto.mimeType,
            status = dto.status,
            url = url,
        )
    }
}
