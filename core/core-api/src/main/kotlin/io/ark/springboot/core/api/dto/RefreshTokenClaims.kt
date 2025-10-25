package io.ark.springboot.core.api.dto

data class RefreshTokenClaims(
    val userId: Long,
    val expiration: Long,
)
