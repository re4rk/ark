package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.LoginRequest
import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.api.controller.v1.request.TokenValidationRequest
import io.ark.springboot.core.api.controller.v1.response.LoginResponse
import io.ark.springboot.core.api.service.AuthApiService
import io.ark.springboot.core.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authApiService: AuthApiService,
) {
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Unit> {
        return ApiResponse.success(authApiService.signUp(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<LoginResponse> {
        return ApiResponse.success(authApiService.login(request))
    }

    @PostMapping("/token/refresh")
    fun refreshToken(@Valid @RequestBody request: TokenValidationRequest): ApiResponse<LoginResponse> {
        return ApiResponse.success(authApiService.refreshToken(request))
    }
}
