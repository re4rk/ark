package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.ExampleRequestDto
import io.ark.springboot.core.domain.ExampleResult
import io.ark.springboot.core.domain.ExampleService
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.NULL
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.requestFields
import io.ark.springboot.test.api.dsl.responseFields
import io.ark.springboot.test.api.dsl.type
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

class ExampleControllerTest : RestDocsTest() {
    private lateinit var exampleService: ExampleService
    private lateinit var controller: ExampleController

    @BeforeEach
    fun setUp() {
        exampleService = mockk()
        controller = ExampleController(exampleService)
        mockMvc = mockController(controller)
    }

    @Test
    fun getExample() {
        every { exampleService.getExample(any()) } returns ExampleResult(1, "BYE")

        given()
            .contentType(ContentType.JSON)
            .get("/examples/{exampleId}", 1)
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "exampleGet",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    RequestDocumentation.pathParameters(
                        parameterWithName("exampleId").description("Example ID"),
                    ),
                    responseFields(
                        "result" type STRING means "결과 타입",
                        "data.id" type NUMBER means "예시 ID",
                        "data.result" type STRING means "결과 데이터",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun postExample() {
        every { exampleService.createExample(any()) } returns ExampleResult(1, "BYE")

        given()
            .contentType(ContentType.JSON)
            .body(ExampleRequestDto("HELLO_BODY"))
            .post("/examples")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "examplePost",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "data" type STRING means "예시 데이터 필드",
                    ),
                    responseFields(
                        "result" type STRING means "결과 타입",
                        "data.id" type NUMBER means "예시 ID",
                        "data.result" type STRING means "결과 데이터",
                        "error" type NULL isIgnored true,
                    ),
                ),
            )
    }
}
