package io.ark.springboot.core.api.provider

import io.ark.springboot.core.api.provider.dto.AccessTokenClaims
import io.ark.springboot.core.api.provider.dto.RefreshTokenClaims

interface TokenProvider {
    fun generateAccessToken(userId: Long): String
    fun generateRefreshToken(userId: Long): String
    fun extractAccessToken(accessToken: String): AccessTokenClaims
    fun extractRefreshToken(refreshToken: String): RefreshTokenClaims
}
