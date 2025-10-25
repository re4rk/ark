package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.LoginRequest
import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.api.controller.v1.request.TokenValidationRequest
import io.ark.springboot.core.api.controller.v1.response.LoginResponse
import io.ark.springboot.core.api.service.AuthApiService
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
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

class AuthControllerTest : RestDocsTest() {

    private lateinit var authApiService: AuthApiService
    private lateinit var controller: AuthController

    @BeforeEach
    fun setUp() {
        authApiService = mockk()
        controller = AuthController(authApiService)
        mockMvc = mockController(controller)
    }

    @Test
    fun `회원가입 성공`() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            username = "testuser",
            password = "password123",
            name = "테스트 사용자",
        )

        every { authApiService.signUp(any()) } returns Unit

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/auth/signup")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-signup-success",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "email" type STRING means "이메일",
                        "username" type STRING means "사용자명",
                        "password" type STRING means "비밀번호",
                        "name" type STRING means "이름",
                    ),
                    responseFields(
                        "result" type STRING means "응답 결과",
                        "data" type STRING isIgnored true,
                        "error" type STRING isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `로그인 성공`() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123",
        )

        val loginResponse = LoginResponse(
            accessToken = "access_token_here",
            refreshToken = "refresh_token_here",
            tokenType = "Bearer",
        )

        every { authApiService.login(any()) } returns loginResponse

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/auth/login")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-login-success",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "email" type STRING means "이메일",
                        "password" type STRING means "비밀번호",
                    ),
                    responseFields(
                        "result" type STRING means "응답 결과",
                        "data.accessToken" type STRING means "액세스 토큰",
                        "data.refreshToken" type STRING means "갱신 토큰",
                        "data.tokenType" type STRING means "토큰 타입",
                        "error" type STRING isIgnored true,
                    ),
                ),
            )
    }

    @Test
    fun `토큰 갱신 성공`() {
        // given
        val request = TokenValidationRequest(
            refreshToken = "refresh_token_here",
        )

        val loginResponse = LoginResponse(
            accessToken = "new_access_token_here",
            refreshToken = "new_refresh_token_here",
            tokenType = "Bearer",
        )

        every { authApiService.refreshToken(any()) } returns loginResponse

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/auth/token/refresh")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-refresh-token-success",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "refreshToken" type STRING means "갱신 토큰",
                    ),
                    responseFields(
                        "result" type STRING means "응답 결과",
                        "data.accessToken" type STRING means "액세스 토큰",
                        "data.refreshToken" type STRING means "갱신 토큰",
                        "data.tokenType" type STRING means "토큰 타입",
                        "error" type STRING isIgnored true,
                    ),
                ),
            )
    }
}
