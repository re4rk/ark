package io.ark.springboot.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank

data class TokenValidationRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val refreshToken: String,
)
