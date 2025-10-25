package io.ark.springboot.core.domain.user.validator

interface PasswordProcessor {
    fun validate(password: String)
    fun encode(password: String): String
    fun matches(password: String, encodedPassword: String): Boolean
}
