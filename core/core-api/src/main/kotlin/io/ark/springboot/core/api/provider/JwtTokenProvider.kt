package io.ark.springboot.core.api.provider

import io.ark.springboot.core.api.config.property.JwtProperties
import io.ark.springboot.core.api.provider.dto.AccessTokenClaims
import io.ark.springboot.core.api.provider.dto.RefreshTokenClaims
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) : TokenProvider {

    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()) }

    override fun generateAccessToken(userId: Long): String {
        val now = Date()

        return Jwts.builder()
            .subject(userId.toString())
            .claim(USER_ID_CLAIM, userId)
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.accessTokenExpiration))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    override fun generateRefreshToken(userId: Long): String {
        val now = Date()

        return Jwts.builder()
            .subject(userId.toString())
            .claim(USER_ID_CLAIM, userId)
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.refreshTokenExpiration))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    override fun extractAccessToken(accessToken: String): AccessTokenClaims {
        val claims = getClaimsFromToken(accessToken)
        val tokenType = claims[TOKEN_TYPE_CLAIM] as String?
        if (tokenType != ACCESS_TOKEN_TYPE) {
            throw CoreException(ErrorType.INVALID_TOKEN, "잘못된 토큰 타입입니다")
        }
        return AccessTokenClaims(
            userId = (claims[USER_ID_CLAIM] as Number).toLong(),
            expiration = claims.expiration.time,
        )
    }

    override fun extractRefreshToken(refreshToken: String): RefreshTokenClaims {
        val claims = getClaimsFromToken(refreshToken)
        val tokenType = claims[TOKEN_TYPE_CLAIM] as String?
        if (tokenType != REFRESH_TOKEN_TYPE) {
            throw CoreException(ErrorType.INVALID_TOKEN, "잘못된 토큰 타입입니다")
        }
        return RefreshTokenClaims(
            userId = (claims[USER_ID_CLAIM] as Number).toLong(),
            expiration = claims.expiration.time,
        )
    }

    private fun getClaimsFromToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            log.warn("만료된 토큰: {}", e.message)
            throw CoreException(ErrorType.EXPIRED_TOKEN, "만료된 토큰입니다")
        } catch (e: UnsupportedJwtException) {
            log.warn("지원되지 않는 토큰: {}", e.message)
            throw CoreException(ErrorType.INVALID_TOKEN, "지원되지 않는 토큰입니다")
        } catch (e: MalformedJwtException) {
            log.warn("잘못된 형식의 토큰: {}", e.message)
            throw CoreException(ErrorType.INVALID_TOKEN, "잘못된 형식의 토큰입니다")
        } catch (e: Exception) {
            log.error("토큰 파싱 실패: {}", e.message)
            throw CoreException(ErrorType.INVALID_TOKEN, "유효하지 않은 토큰입니다")
        }
    }

    companion object {
        private const val USER_ID_CLAIM = "userId"
        private const val TOKEN_TYPE_CLAIM = "tokenType"
        private const val ACCESS_TOKEN_TYPE = "access"
        private const val REFRESH_TOKEN_TYPE = "refresh"
    }
}
