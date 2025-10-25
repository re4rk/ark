package io.ark.springboot.core.api.dto

data class AccessTokenClaims(
    val userId: Long,
    val expiration: Long,
)
