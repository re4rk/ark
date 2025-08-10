package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.file.FileDto
import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.NULL
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.responseFields
import io.ark.springboot.test.api.dsl.type
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import java.time.LocalDateTime

class FileControllerTest : RestDocsTest() {
    private lateinit var fileService: FileService
    private lateinit var controller: FileController

    @BeforeEach
    fun setUp() {
        fileService = mockk()
        controller = FileController(fileService)
        mockMvc = mockController(controller)
    }

    @Test
    fun `파일 업로드 성공`() {
        // given
        val fileContent = "hello world".toByteArray()
        val fileName = "test.txt"
        val now = LocalDateTime.now()
        val fileDto = FileDto(
            id = 1L,
            originalName = fileName,
            key = "uploads/123456789-test.txt",
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            createdAt = now,
            updatedAt = now,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = fileName,
                contentType = "text/plain",
            )
        } returns fileDto

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", fileName, fileContent, "text/plain")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-upload",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.fileId" type STRING means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.createdAt" type STRING means "생성 시각",
                        "data.updatedAt" type STRING means "수정 시각",
                        "data.url" type NULL means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일 다운로드 URL 조회 성공`() {
        // given
        val key = "test.txt"
        val presignedUrl = "https://example.com/presigned-url"
        every { fileService.getDownloadUrl(key) } returns presignedUrl

        // when & then
        given()
            .`when`()
            .get("/api/v1/files/{key}/download-url", key)
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-download-url",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    RequestDocumentation.pathParameters(
                        parameterWithName("key").description("파일 키"),
                    ),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.url" type STRING means "미리 서명된 다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일명이 없는 파일 업로드`() {
        // given
        val fileContent = "hello world".toByteArray()
        val now = LocalDateTime.now()
        val fileDto = FileDto(
            id = 1L,
            originalName = "unknown",
            key = "uploads/123456789-unknown",
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            createdAt = now,
            updatedAt = now,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = any(),
                contentType = any(),
            )
        } returns fileDto

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", null as String?, fileContent, "text/plain")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .status(HttpStatus.OK)
            .body("data.originalName", org.hamcrest.Matchers.equalTo("unknown"))
            .apply(
                document(
                    "file-upload-no-name",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.fileId" type STRING means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.createdAt" type STRING means "생성 시각",
                        "data.updatedAt" type STRING means "수정 시각",
                        "data.url" type NULL means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `대용량 파일 업로드`() {
        // given
        val largeContent = ByteArray(1024 * 1024) { it.toByte() } // 1MB
        val fileName = "large-file.dat"
        val now = LocalDateTime.now()
        val fileDto = FileDto(
            id = 1L,
            originalName = fileName,
            key = "uploads/123456789-large-file.dat",
            size = largeContent.size.toLong(),
            mimeType = "application/octet-stream",
            status = UploadStatus.UPLOADED,
            createdAt = now,
            updatedAt = now,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = fileName,
                contentType = "application/octet-stream",
            )
        } returns fileDto

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", fileName, largeContent, "application/octet-stream")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .status(HttpStatus.OK)
            .body("data.size", org.hamcrest.Matchers.equalTo(largeContent.size))
            .apply(
                document(
                    "file-upload-large",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.fileId" type STRING means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.createdAt" type STRING means "생성 시각",
                        "data.updatedAt" type STRING means "수정 시각",
                        "data.url" type NULL means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일 상태 조회 성공`() {
        // given
        val fileId = "1"
        val now = LocalDateTime.now()
        val fileDto = FileDto(
            id = 1L,
            originalName = "test.txt",
            key = "uploads/123456789-test.txt",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            createdAt = now,
            updatedAt = now,
        )
        val presignedUrl = "https://example.com/presigned-url"
        every { fileService.getFileStatus(1L) } returns fileDto
        every { fileService.getDownloadUrl(fileDto.key) } returns presignedUrl

        // when & then
        given()
            .`when`()
            .get("/api/v1/files/{fileId}/status", fileId)
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-status",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    RequestDocumentation.pathParameters(
                        parameterWithName("fileId").description("파일 ID"),
                    ),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.fileId" type STRING means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.createdAt" type STRING means "생성 시각",
                        "data.updatedAt" type STRING means "수정 시각",
                        "data.url" type STRING means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }
}
