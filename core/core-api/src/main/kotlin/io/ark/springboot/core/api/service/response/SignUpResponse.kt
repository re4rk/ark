package io.ark.springboot.core.api.service.response

data class SignUpResponse(
    val id: Long,
    val email: String,
    val username: String,
    val name: String,
)
