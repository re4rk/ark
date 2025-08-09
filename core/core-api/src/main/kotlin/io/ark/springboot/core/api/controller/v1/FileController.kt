package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.support.response.ApiResponse
import io.ark.springboot.client.s3.S3Client
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val s3Client: S3Client,
) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("description", required = false) description: String?,
    ): ApiResponse<FileUploadResponse> {
        val result = s3Client.upload(
            bytes = file.bytes,
            originalFilename = file.originalFilename ?: "unknown",
            contentType = file.contentType,
        )
        return ApiResponse.success(
            FileUploadResponse(
                fileId = result.key,
                originalName = result.originalName,
                size = result.size,
                mimeType = result.mimeType,
                uploadedAt = result.uploadedAt,
                url = result.downloadPath,
            ),
        )
    }

    @GetMapping("/{key}/download")
    fun download(@PathVariable("key") key: String): ResponseEntity<ByteArray> {
        val obj = s3Client.download(key)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$key\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(obj)
    }
}

data class FileUploadResponse(
    val fileId: String,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val uploadedAt: String,
    val url: String,
)
