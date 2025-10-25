package io.ark.springboot.core.domain.user

data class UserData(
    val email: String,
    val username: String,
    val password: String,
    val name: String,
) {
    companion object {
        fun from(user: User) = UserData(
            email = user.email,
            username = user.username,
            password = user.encodedPassword,
            name = user.name,
        )
    }
}
