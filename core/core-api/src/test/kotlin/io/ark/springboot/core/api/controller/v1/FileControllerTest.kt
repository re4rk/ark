package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.file.FileDto
import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.storage.db.core.file.FileCategory
import io.ark.springboot.storage.db.core.file.UploadStatus
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.NULL
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.responseFields
import io.ark.springboot.test.api.dsl.type
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
    fun `파일 업로드 성공`() = runTest {
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
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )
        coEvery {
            fileService.uploadFile(
                file = any(),
                category = FileCategory.IMAGE,
                uploaderId = 1L,
            )
        } returns fileDto

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", fileName, fileContent, "text/plain")
            .param("category", FileCategory.IMAGE.name)
            .param("uploaderId", "1")
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
                        "data.id" type NUMBER means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.url" type NULL means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일 다운로드 URL 조회 성공`() {
        // given
        val id = 1L
        val presignedUrl = "https://example.com/presigned-url"
        coEvery { fileService.getDownloadUrl(id) } returns presignedUrl

        // when & then
        given()
            .`when`()
            .get("/api/v1/files/{fileId}/url", id)
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-download-url",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    RequestDocumentation.pathParameters(
                        parameterWithName("fileId").description("파일 ID"),
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
    fun `파일명이 없는 파일 업로드`() = runTest {
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
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )
        coEvery {
            fileService.uploadFile(
                file = any(),
                category = FileCategory.IMAGE,
                uploaderId = 1L,
            )
        } returns fileDto

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", "unknown", fileContent, "text/plain")
            .param("category", FileCategory.IMAGE.name)
            .param("uploaderId", "1")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-upload-no-filename",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        "result" type STRING means "성공 여부",
                        "data.id" type NUMBER means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.url" type NULL means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일 상태 조회 성공`() {
        // given
        val fileId = 1L
        val now = LocalDateTime.now()
        val fileDto = FileDto(
            id = fileId,
            originalName = "test.txt",
            key = "uploads/123456789-test.txt",
            size = 100L,
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
            createdAt = now,
            updatedAt = now,
            category = FileCategory.IMAGE,
            uploaderId = 1L,
        )
        val presignedUrl = "https://example.com/presigned-url"

        coEvery { fileService.getFileStatus(fileId) } returns fileDto
        coEvery { fileService.getDownloadUrl(fileId) } returns presignedUrl

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
                        "data.id" type NUMBER means "파일 ID",
                        "data.originalName" type STRING means "원본 파일명",
                        "data.size" type NUMBER means "파일 크기",
                        "data.mimeType" type STRING means "MIME 타입",
                        "data.status" type STRING means "업로드 상태",
                        "data.url" type STRING means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }
}
