package io.ark.springboot.core.api.controller.v1.request

import io.ark.springboot.core.domain.user.UserData
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 128, message = "비밀번호는 8자 이상 128자 이하여야 합니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    val name: String,
) {
    fun toUserData() = UserData(
        email = email,
        username = username,
        password = password,
        name = name,
    )
}
