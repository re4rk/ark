package io.ark.springboot.core.api.provider.dto

data class RefreshTokenClaims(
    val userId: Long,
    val expiration: Long,
)
