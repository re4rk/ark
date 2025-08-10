package io.ark.springboot.core.api.controller.v1.request

import org.springframework.web.multipart.MultipartFile

class UploadFileRequest(
    val file: MultipartFile,
    val category: String,
    val uploaderId: Long,
)
