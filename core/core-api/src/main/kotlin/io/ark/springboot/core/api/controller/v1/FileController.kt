package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.response.FileDownloadUrlResponse
import io.ark.springboot.core.api.controller.v1.response.FileResponse
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
    fun getStatus(@PathVariable("fileId") fileId: Long): ApiResponse<FileResponse> {
        val fileDto = fileService.getFileStatus(fileId)
        val url = if (fileDto.status == UploadStatus.UPLOADED) {
            fileService.getDownloadUrl(fileId)
        } else {
            null
        }
        return ApiResponse.success(FileResponse.from(fileDto, url))
    }

    @GetMapping("/{fileId}/url")
    fun getDownloadUrl(@PathVariable("fileId") fileId: Long): ApiResponse<FileDownloadUrlResponse> {
        val url = fileService.getDownloadUrl(fileId)
        return ApiResponse.success(FileDownloadUrlResponse(url))
    }
}

