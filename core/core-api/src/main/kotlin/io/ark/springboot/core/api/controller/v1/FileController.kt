package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.core.support.response.ApiResponse
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
    private val fileService: FileService,
) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<FileUploadResponse> {
        // 파일 타입/크기 검증
        if (!fileService.validateFileType(file.contentType ?: "")) {
            throw IllegalArgumentException("지원하지 않는 파일 형식입니다")
        }
        if (!fileService.validateFileSize(file.size)) {
            throw IllegalArgumentException("파일 크기가 제한을 초과했습니다")
        }
        // S3 업로드 및 메타데이터 저장
        val entity = fileService.uploadFile(
            bytes = file.bytes,
            originalName = file.originalFilename ?: "unknown",
            contentType = file.contentType,
        )
        return ApiResponse.success(
            FileUploadResponse(
                originalName = entity.originalName,
                size = entity.size,
                mimeType = entity.mimeType,
                uploadedAt = entity.createdAt.toString(),
                url = "/api/v1/files/${entity.id}/download",
                fileId = entity.id.toString(),
            ),
        )
    }

    @GetMapping("/{key}/download")
    fun download(@PathVariable("key") key: String): ResponseEntity<ByteArray> {
        val obj = fileService.downloadFile(key)
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
