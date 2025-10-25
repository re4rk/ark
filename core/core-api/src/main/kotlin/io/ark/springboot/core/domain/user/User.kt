package io.ark.springboot.core.domain.user

data class User(
    val id: Long,
    val email: String,
    val username: String,
    val encodedPassword: String,
    val name: String,
)
