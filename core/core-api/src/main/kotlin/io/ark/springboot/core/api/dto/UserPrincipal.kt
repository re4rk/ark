package io.ark.springboot.core.api.dto

import io.ark.springboot.core.domain.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UserPrincipal(
    private val user: User,
) : UserDetails {

    val id: Long get() = user.id
    val email: String get() = user.email
    val name: String get() = user.name

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String {
        return user.encodedPassword
    }

    override fun getUsername(): String {
        return user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    companion object {
        fun from(user: User): UserPrincipal {
            return UserPrincipal(user)
        }
    }
}
