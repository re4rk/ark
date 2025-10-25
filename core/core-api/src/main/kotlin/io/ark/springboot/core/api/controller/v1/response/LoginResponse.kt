package io.ark.springboot.core.api.controller.v1.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)
