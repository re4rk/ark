package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.UploadFileRequest
import io.ark.springboot.core.api.controller.v1.response.FileResponse
import io.ark.springboot.core.api.controller.v1.response.GetFileUrlResponse
import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.core.domain.file.UploadFileService
import io.ark.springboot.core.support.response.ApiResponse
import io.ark.springboot.storage.db.core.file.UploadStatus
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileService: FileService,
    private val uploadFileService: UploadFileService,
) {
    private val logger = LoggerFactory.getLogger(FileController::class.java)

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@ModelAttribute uploadFileRequest: UploadFileRequest): ApiResponse<FileResponse> = runBlocking {
        val file = uploadFileService.uploadFile(
            multipartFile = uploadFileRequest.file,
            category = uploadFileRequest.category,
            uploaderId = uploadFileRequest.uploaderId,
        )
        return@runBlocking ApiResponse.success(FileResponse.from(file, null))
    }

    @GetMapping("/{fileId}/status")
    fun getStatus(@PathVariable("fileId") fileId: Long): ApiResponse<FileResponse> {
        val file = fileService.findById(fileId)
        val url = if (file.status == UploadStatus.UPLOADED) {
            fileService.getDownloadUrl(fileId)
        } else {
            null
        }
        return ApiResponse.success(FileResponse.from(file, url))
    }

    @GetMapping("/{fileId}/url")
    fun getDownloadUrl(@PathVariable("fileId") fileId: Long): ApiResponse<GetFileUrlResponse> {
        val url = fileService.getDownloadUrl(fileId)
        return ApiResponse.success(GetFileUrlResponse(url))
    }
}
