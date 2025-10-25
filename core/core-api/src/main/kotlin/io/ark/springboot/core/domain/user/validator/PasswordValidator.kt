package io.ark.springboot.core.domain.user.validator

interface PasswordValidator {
    fun validate(password: String)
}
