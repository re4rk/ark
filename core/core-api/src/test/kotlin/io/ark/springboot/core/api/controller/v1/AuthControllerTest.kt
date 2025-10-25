package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.domain.user.User
import io.ark.springboot.core.domain.user.UserSignUpService
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document

class AuthControllerTest : RestDocsTest() {

    private lateinit var userSignUpService: UserSignUpService
    private lateinit var controller: AuthController

    @BeforeEach
    fun setUp() {
        userSignUpService = mockk()
        controller = AuthController(userSignUpService)
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

        val user = User(
            id = 1L,
            email = request.email,
            username = request.username,
            name = request.name,
            password = "hashed_password",
        )

        every { userSignUpService.signUp(any()) } returns user

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
                        "data" type NUMBER means "생성된 사용자 ID",
                        "error" type STRING isIgnored true,
                    ),
                ),
            )
    }

}
