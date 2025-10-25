package io.ark.springboot.core.api.service.provider

import io.ark.springboot.core.api.config.property.JwtProperties
import io.ark.springboot.core.api.service.provider.JwtTokenProvider
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import java.util.Date

@ExtendWith(MockitoExtension::class)
@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties

    private val log = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        jwtProperties = JwtProperties(
            secret = "test-secret-key-for-jwt-token-generation-and-validation-please-change-in-production",
            accessTokenExpiration = 3600000L, // 1시간
            refreshTokenExpiration = 604800000L, // 7일
        )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
    }

    @Test
    fun `AccessToken 생성 테스트`() {
        // given
        val userId = 1L

        // when
        val accessToken = jwtTokenProvider.generateAccessToken(userId)

        // then
        assertNotNull(accessToken)
        assertTrue(accessToken.isNotEmpty())

        // 토큰에서 클레임 추출 가능한지 확인
        val claims = jwtTokenProvider.extractAccessToken(accessToken)
        assertEquals(userId, claims.userId)
        assertTrue(claims.expiration > System.currentTimeMillis())
        log.info("accessToken: {}", accessToken)
    }

    @Test
    fun `RefreshToken 생성 테스트`() {
        // given
        val userId = 1L

        // when
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        // then
        assertNotNull(refreshToken)
        assertTrue(refreshToken.isNotEmpty())

        // 토큰에서 클레임 추출 가능한지 확인
        val claims = jwtTokenProvider.extractRefreshToken(refreshToken)
        assertEquals(userId, claims.userId)
        assertTrue(claims.expiration > System.currentTimeMillis())
        log.info("refreshToken: {}", refreshToken)
    }

    @Test
    fun `AccessToken 클레임 추출 테스트`() {
        // given
        val userId = 1L
        val accessToken = jwtTokenProvider.generateAccessToken(userId)

        // when
        val claims = jwtTokenProvider.extractAccessToken(accessToken)

        // then
        assertEquals(userId, claims.userId)
        assertTrue(claims.expiration > System.currentTimeMillis())
    }

    @Test
    fun `잘못된 토큰에서 AccessToken 클레임 추출 시 예외 발생`() {
        // given
        val invalidToken = "invalid.token.here"

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractAccessToken(invalidToken)
        }

        assertEquals(ErrorType.INVALID_TOKEN, exception.errorType)
        assertEquals("유효하지 않은 토큰입니다.", exception.message)
    }

    @Test
    fun `만료된 토큰에서 AccessToken 클레임 추출 시 예외 발생`() {
        // given
        val userId = 1L
        val expiredToken = createExpiredToken(userId)

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractAccessToken(expiredToken)
        }

        assertEquals(ErrorType.EXPIRED_TOKEN, exception.errorType)
        assertEquals("만료된 토큰입니다.", exception.message)
    }

    @Test
    fun `RefreshToken 클레임 추출 테스트`() {
        // given
        val userId = 1L
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        // when
        val claims = jwtTokenProvider.extractRefreshToken(refreshToken)

        // then
        assertEquals(userId, claims.userId)
        assertTrue(claims.expiration > System.currentTimeMillis())
    }

    @Test
    fun `잘못된 토큰에서 RefreshToken 클레임 추출 시 예외 발생`() {
        // given
        val invalidToken = "invalid.token.here"

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractRefreshToken(invalidToken)
        }

        assertEquals(ErrorType.INVALID_TOKEN, exception.errorType)
        assertEquals("유효하지 않은 토큰입니다.", exception.message)
    }

    @Test
    fun `만료된 토큰에서 RefreshToken 클레임 추출 시 예외 발생`() {
        // given
        val userId = 1L
        val expiredToken = createExpiredToken(userId)

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractRefreshToken(expiredToken)
        }

        assertEquals(ErrorType.EXPIRED_TOKEN, exception.errorType)
        assertEquals("만료된 토큰입니다.", exception.message)
    }

    @Test
    fun `AccessToken을 RefreshToken으로 추출 시 예외 발생`() {
        // given
        val userId = 1L
        val accessToken = jwtTokenProvider.generateAccessToken(userId)

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractRefreshToken(accessToken)
        }

        assertEquals(ErrorType.INVALID_TOKEN, exception.errorType)
        assertEquals("유효하지 않은 토큰입니다.", exception.message)
    }

    @Test
    fun `RefreshToken을 AccessToken으로 추출 시 예외 발생`() {
        // given
        val userId = 1L
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        // when & then
        val exception = assertThrows<CoreException> {
            jwtTokenProvider.extractAccessToken(refreshToken)
        }

        assertEquals(ErrorType.INVALID_TOKEN, exception.errorType)
        assertEquals("유효하지 않은 토큰입니다.", exception.message)
    }

    @Test
    fun `AccessToken 클레임의 만료시간이 올바른지 확인`() {
        // given
        val userId = 1L
        val accessToken = jwtTokenProvider.generateAccessToken(userId)
        val now = System.currentTimeMillis()

        // when
        val claims = jwtTokenProvider.extractAccessToken(accessToken)

        // then
        assertTrue(claims.expiration > now)
        assertTrue(claims.expiration <= now + jwtProperties.accessTokenExpiration + 1000) // 1초 오차 허용
    }

    @Test
    fun `RefreshToken 클레임의 만료시간이 올바른지 확인`() {
        // given
        val userId = 1L
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)
        val now = System.currentTimeMillis()

        // when
        val claims = jwtTokenProvider.extractRefreshToken(refreshToken)

        // then
        assertTrue(claims.expiration > now)
        assertTrue(claims.expiration <= now + jwtProperties.refreshTokenExpiration + 1000) // 1초 오차 허용
    }

    private fun createExpiredToken(userId: Long): String {
        val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
        val pastDate = Date(System.currentTimeMillis() - 3600000) // 1시간 전

        return Jwts.builder()
            .subject(userId.toString())
            .claim("userId", userId)
            .claim("tokenType", "access")
            .issuedAt(pastDate)
            .expiration(pastDate) // 이미 만료된 시간으로 설정
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }
}
