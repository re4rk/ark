package io.ark.springboot.core.api.provider.dto

data class AccessTokenClaims(
    val userId: Long,
    val expiration: Long,
)
