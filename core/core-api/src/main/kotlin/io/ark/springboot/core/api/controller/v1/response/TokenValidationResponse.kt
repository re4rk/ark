package io.ark.springboot.core.api.controller.v1.response

data class TokenValidationResponse(
    val isValid: Boolean,
    val tokenType: String,
)
