package io.ark.springboot.core.domain.user

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val password: String,
    val name: String,
)
