package io.ark.springboot.core.api.controller.v1.request

import io.ark.springboot.storage.db.core.file.FileCategory
import org.springframework.web.multipart.MultipartFile

class UploadFileRequest(
    val file: MultipartFile,
    val category: FileCategory,
    val uploaderId: Long,
)
