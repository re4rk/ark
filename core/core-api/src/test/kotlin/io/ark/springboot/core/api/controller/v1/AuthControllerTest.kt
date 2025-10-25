package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.TokenValidationRequest
import io.ark.springboot.core.api.dto.UserPrincipal
import io.ark.springboot.core.api.service.AuthApiService
import io.ark.springboot.core.api.service.request.LoginRequest
import io.ark.springboot.core.api.service.request.SignUpRequest
import io.ark.springboot.core.api.service.request.UserPasswordChangeRequest
import io.ark.springboot.core.api.service.response.LoginResponse
import io.ark.springboot.core.domain.user.User
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.requestFields
import io.ark.springboot.test.api.dsl.responseFields
import io.ark.springboot.test.api.dsl.type
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.ContentType
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class AuthControllerTest : RestDocsTest() {

    private lateinit var authApiService: AuthApiService
    private lateinit var controller: AuthController

    @BeforeEach
    fun setUp() {
        authApiService = mockk()
        controller = AuthController(authApiService)
        mockMvc = mockControllerWithAuth(controller)
    }

    private fun mockControllerWithAuth(controller: Any): MockMvcRequestSpecification {
        val user = User(
            id = 1L,
            email = "test@example.com",
            username = "testuser",
            encodedPassword = "encoded_password",
            name = "테스트 사용자",
        )
        val userPrincipal = UserPrincipal.from(user)

        val authentication = UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER")),
        )

        SecurityContextHolder.getContext().authentication = authentication

        return mockController(controller, TestUserPrincipalResolver())
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
    fun `현재 사용자 정보 조회 성공`() {
        // when & then
        given()
            .get("/api/v1/auth/me")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-me-success",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseFields(
                        "result" type STRING means "응답 결과",
                        "data.id" type NUMBER means "사용자 ID",
                        "data.email" type STRING means "이메일",
                        "data.name" type STRING means "이름",
                        "data.userName" type STRING means "사용자명",
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

    @Test
    fun `비밀번호 변경 성공`() {
        // given
        val request = UserPasswordChangeRequest(
            password = "old_password",
            newPassword = "new_password123",
        )

        every { authApiService.changePassword(any(), any()) } returns Unit

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/auth/password/change")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    "auth-change-password-success",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        "password" type STRING means "현재 비밀번호",
                        "newPassword" type STRING means "새 비밀번호",
                    ),
                    responseFields(
                        "result" type STRING means "응답 결과",
                        "data" type STRING isIgnored true,
                        "error" type STRING isIgnored true,
                    ),
                ),
            )
    }
}

class TestUserPrincipalResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType.simpleName == "UserPrincipal"
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return authentication?.principal
    }
}
