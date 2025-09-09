package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.SignUpRequest
import io.ark.springboot.core.domain.user.UserService
import io.ark.springboot.core.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
) {
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> {
        val user = userService.signUp(request.toUserData())
        return ApiResponse.success(user.id)
    }
}
