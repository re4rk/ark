package io.ark.springboot.core.api.service.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserPasswordChangeRequest(
    val password: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 128, message = "비밀번호는 8자 이상 128자 이하여야 합니다")
    val newPassword: String,
) {
    companion object {
        fun from(request: UserPasswordChangeRequest) = UserPasswordChangeRequest(
            password = request.password,
            newPassword = request.newPassword,
        )
    }
}
