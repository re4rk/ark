package io.ark.springboot.core.api.filter

import io.ark.springboot.core.api.dto.UserPrincipal
import io.ark.springboot.core.api.service.provider.TokenProvider
import io.ark.springboot.core.domain.user.storage.UserStorage
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val userStorage: UserStorage,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = extractTokenFromRequest(request)
            if (token != null) {
                val accessTokenClaims = tokenProvider.extractAccessToken(token)
                val user = userStorage.getUser(accessTokenClaims.userId)
                val userPrincipal = UserPrincipal.Companion.from(user)

                val authentication = UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.authorities,
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            log.debug("JWT 인증 실패: {}", e.message)
            // 인증 실패 시 SecurityContext를 비우지 않고 계속 진행
            // 이렇게 하면 인증이 필요한 엔드포인트에서만 401 에러가 발생
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            null
        }
    }
}
