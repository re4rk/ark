package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.file.FileService
import io.ark.springboot.storage.db.core.FileEntity
import io.ark.springboot.storage.db.core.UploadStatus
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
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

class FileControllerTest : RestDocsTest() {
    private lateinit var fileService: FileService
    private lateinit var controller: FileController

    @BeforeEach
    fun setUp() {
        fileService = mockk()
        every { fileService.validateFileType(any()) } returns true
        every { fileService.validateFileSize(any()) } returns true
        controller = FileController(fileService)
        mockMvc = mockController(controller)
    }

    @Test
    fun `파일 업로드 성공`() {
        // given
        val fileContent = "hello world".toByteArray()
        val fileName = "test.txt"
        val fileEntity = FileEntity(
            originalName = fileName,
            s3Key = "uploads/123456789-test.txt",
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = fileName,
                contentType = "text/plain",
            )
        } returns fileEntity

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", fileName, fileContent, "text/plain")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .also { response ->
                if (response.extract().statusCode() != 200) {
                    println("Expected 200 but got: ${response.extract().statusCode()}")
                }
                println("Response body: ${response.extract().body().asString()}")
            }
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
                        "data.uploadedAt" type STRING means "업로드 시각",
                        "data.url" type STRING means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `파일 다운로드 성공`() {
        // given
        val key = "test.txt" // 단순한 파일명으로 변경
        val fileContent = "hello world".toByteArray()
        every { fileService.downloadFile(key) } returns fileContent

        // when & then
        given()
            .`when`()
            .get("/api/v1/files/{key}/download", key)
            .then()
            .status(HttpStatus.OK)
            .header("Content-Disposition", "attachment; filename=\"$key\"")
            .apply(
                document(
                    "file-download",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    RequestDocumentation.pathParameters(
                        parameterWithName("key").description("파일 키"),
                    ),
                    HeaderDocumentation.responseHeaders(
                        HeaderDocumentation.headerWithName("Content-Disposition").description("첨부파일 헤더"),
                    ),
                ),
            )
    }

    @Test
    fun `파일명이 없는 파일 업로드`() {
        // given
        val fileContent = "hello world".toByteArray()
        val fileEntity = FileEntity(
            originalName = "unknown",
            s3Key = "uploads/123456789-unknown",
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            status = UploadStatus.UPLOADED,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = "",
                contentType = "text/plain",
            )
        } returns fileEntity

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
                        "data.uploadedAt" type STRING means "업로드 시각",
                        "data.url" type STRING means "다운로드 URL",
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
        val fileEntity = FileEntity(
            originalName = fileName,
            s3Key = "uploads/123456789-large-file.dat",
            size = largeContent.size.toLong(),
            mimeType = "application/octet-stream",
            status = UploadStatus.UPLOADED,
        )
        every {
            fileService.uploadFile(
                bytes = any(),
                originalName = fileName,
                contentType = "application/octet-stream",
            )
        } returns fileEntity

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
                        "data.uploadedAt" type STRING means "업로드 시각",
                        "data.url" type STRING means "다운로드 URL",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }
}
