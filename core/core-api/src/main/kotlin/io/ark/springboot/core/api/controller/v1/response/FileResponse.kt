package io.ark.springboot.core.api.controller.v1.response

import io.ark.springboot.core.domain.file.File
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
        fun from(file: File, url: String?) = FileResponse(
            id = file.id,
            originalName = file.originalName,
            size = file.size,
            mimeType = file.mimeType,
            status = file.status,
            url = url,
        )
    }
}
