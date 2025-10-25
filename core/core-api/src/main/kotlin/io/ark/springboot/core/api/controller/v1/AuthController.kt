package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.domain.user.UserSignUpService
import io.ark.springboot.core.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userSignUpService: UserSignUpService,
) {
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> {
        val user = userSignUpService.signUp(request.toUserData())
        return ApiResponse.success(user.id)
    }
}
