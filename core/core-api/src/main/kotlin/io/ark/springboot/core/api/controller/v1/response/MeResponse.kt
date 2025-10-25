package io.ark.springboot.core.api.controller.v1.response

import io.ark.springboot.core.api.dto.UserPrincipal

data class MeResponse(
    val id: Long,
    val email: String,
    val name: String,
    val userName: String,
) {
    companion object {
        fun from(userPrincipal: UserPrincipal): MeResponse {
            return MeResponse(
                id = userPrincipal.id,
                email = userPrincipal.email,
                name = userPrincipal.name,
                userName = userPrincipal.username,
            )
        }
    }
}
