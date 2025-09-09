package io.ark.springboot.core.api.controller.v1.response

data class SignUpResponse(
    val id: Long,
    val email: String,
    val username: String,
    val name: String,
)
