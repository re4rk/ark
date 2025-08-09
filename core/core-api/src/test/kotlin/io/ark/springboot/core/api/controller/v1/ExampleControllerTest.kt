package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.ExampleRequestDto
import io.ark.springboot.core.domain.ExampleResult
import io.ark.springboot.core.domain.ExampleService
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters

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
                        fieldWithPath("result").type(JsonFieldType.STRING).description("ResultType"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("Example ID"),
                        fieldWithPath("data.result").type(JsonFieldType.STRING).description("Result Data"),
                        fieldWithPath("error").type(JsonFieldType.NULL).ignored(),
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
                        fieldWithPath("data").type(JsonFieldType.STRING).description("ExampleBody Data Field"),
                    ),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).description("ResultType"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("Example ID"),
                        fieldWithPath("data.result").type(JsonFieldType.STRING).description("Result Data"),
                        fieldWithPath("error").type(JsonFieldType.NULL).ignored(),
                    ),
                ),
            )
    }
}
