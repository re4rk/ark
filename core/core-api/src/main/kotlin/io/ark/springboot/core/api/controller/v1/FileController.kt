package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.file.FileDto
import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.core.support.response.ApiResponse
import io.ark.springboot.storage.db.core.file.UploadStatus
import org.springframework.http.MediaType
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
    private val fileService: FileService,
) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<FileResponse> {
        val fileDto = fileService.uploadFile(
            bytes = file.bytes,
            originalName = file.originalFilename ?: "unknown",
            contentType = file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE,
        )
        return ApiResponse.success(FileResponse.from(fileDto, null))
    }

    @GetMapping("/{fileId}/status")
    fun getStatus(@PathVariable("fileId") fileId: String): ApiResponse<FileResponse> {
        val fileDto = fileService.getFileStatus(fileId.toLong())
        val url = if (fileDto.status == UploadStatus.UPLOADED) {
            fileService.getDownloadUrl(fileDto.key)
        } else {
            null
        }
        return ApiResponse.success(FileResponse.from(fileDto, url))
    }

    @GetMapping("/{key}/url")
    fun getDownloadUrl(@PathVariable("key") key: String): ApiResponse<FileDownloadUrlResponse> {
        val url = fileService.getDownloadUrl(key)
        return ApiResponse.success(FileDownloadUrlResponse(url))
    }
}

data class FileResponse(
    val id: Long,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val status: UploadStatus,
    val url: String?,
) {
    companion object {
        fun from(dto: FileDto, url: String?) = FileResponse(
            id = dto.id,
            originalName = dto.originalName,
            size = dto.size,
            mimeType = dto.mimeType,
            status = dto.status,
            url = url,
        )
    }
}

data class FileDownloadUrlResponse(
    val url: String,
)
