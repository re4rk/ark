package io.ark.springboot.core.api.service

import io.ark.springboot.core.api.controller.v1.request.LoginRequest
import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.api.controller.v1.request.TokenValidationRequest
import io.ark.springboot.core.api.controller.v1.response.LoginResponse
import io.ark.springboot.core.api.provider.TokenProvider
import io.ark.springboot.core.domain.user.UserLoginService
import io.ark.springboot.core.domain.user.UserSignUpService
import org.springframework.stereotype.Service

@Service
class AuthApiService(
    private val userSignUpService: UserSignUpService,
    private val userLoginService: UserLoginService,
    private val tokenProvider: TokenProvider,
) {
    fun signUp(request: SignUpRequest) {
        userSignUpService.signUp(request.toUserData())
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userLoginService.login(request.email, request.password)

        val accessToken = tokenProvider.generateAccessToken(user.id)
        val refreshToken = tokenProvider.generateRefreshToken(user.id)

        return LoginResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun refreshToken(request: TokenValidationRequest): LoginResponse {
        val refreshTokenClaims = tokenProvider.extractRefreshToken(request.refreshToken)

        val accessToken = tokenProvider.generateAccessToken(refreshTokenClaims.userId)
        val refreshToken = tokenProvider.generateRefreshToken(refreshTokenClaims.userId)

        return LoginResponse(accessToken = accessToken, refreshToken = refreshToken)
    }
}
