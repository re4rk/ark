package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.client.s3.S3Client
import io.ark.springboot.client.s3.StorageUploadResult
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.BOOLEAN
import io.ark.springboot.test.api.dsl.NULL
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.requestFields
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
import java.time.OffsetDateTime

class FileControllerTest : RestDocsTest() {
    private lateinit var s3Client: S3Client
    private lateinit var controller: FileController

    @BeforeEach
    fun setUp() {
        s3Client = mockk()
        controller = FileController(s3Client)
        mockMvc = mockController(controller)
    }

    @Test
    fun `파일 업로드 성공`() {
        // given
        val fileContent = "hello world".toByteArray()
        val fileName = "test.txt"
        val mockResult = StorageUploadResult(
            key = "uploads/123456789-test.txt",
            originalName = fileName,
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            uploadedAt = OffsetDateTime.now().toString(),
            downloadPath = "/api/v1/files/uploads%2F123456789-test.txt/download",
        )
        every {
            s3Client.upload(
                bytes = any(),
                originalFilename = fileName,
                contentType = "text/plain",
            )
        } returns mockResult

        // when & then
        given()
            .contentType("multipart/form-data")
            .multiPart("file", fileName, fileContent, "text/plain")
            .multiPart("description", "테스트 파일")
            .`when`()
            .post("/api/v1/files/upload")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "file-upload",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "file" type STRING means "업로드 파일",
                        "description" type STRING means "파일 설명" isOptional true,
                    ),
                    responseFields(
                        "success" type BOOLEAN means "성공 여부",
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
        val key = "uploads/123456789-test.txt"
        val fileContent = "hello world".toByteArray()
        every { s3Client.download(key) } returns fileContent

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
        val mockResult = StorageUploadResult(
            key = "uploads/123456789-unknown",
            originalName = "unknown",
            size = fileContent.size.toLong(),
            mimeType = "text/plain",
            uploadedAt = OffsetDateTime.now().toString(),
            downloadPath = "/api/v1/files/uploads%2F123456789-unknown/download",
        )
        every {
            s3Client.upload(
                bytes = any(),
                originalFilename = "unknown",
                contentType = "text/plain",
            )
        } returns mockResult

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
                    requestFields(
                        "file" type STRING means "업로드 파일",
                    ),
                    responseFields(
                        "success" type BOOLEAN means "성공 여부",
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
        val mockResult = StorageUploadResult(
            key = "uploads/123456789-large-file.dat",
            originalName = fileName,
            size = largeContent.size.toLong(),
            mimeType = "application/octet-stream",
            uploadedAt = OffsetDateTime.now().toString(),
            downloadPath = "/api/v1/files/uploads%2F123456789-large-file.dat/download",
        )
        every {
            s3Client.upload(
                bytes = any(),
                originalFilename = fileName,
                contentType = "application/octet-stream",
            )
        } returns mockResult

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
                    requestFields(
                        "file" type STRING means "업로드 파일",
                    ),
                    responseFields(
                        "success" type BOOLEAN means "성공 여부",
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
