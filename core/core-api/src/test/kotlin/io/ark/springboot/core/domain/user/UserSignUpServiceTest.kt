package io.ark.springboot.core.domain.user

import io.ark.springboot.core.domain.user.storage.UserStorage
import io.ark.springboot.core.domain.user.validator.PasswordProcessor
import io.ark.springboot.core.support.error.CoreException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserSignUpServiceTest {

    private lateinit var userStorage: UserStorage
    private lateinit var passwordProcessor: PasswordProcessor
    private lateinit var userSignUpService: UserSignUpService

    @BeforeEach
    fun setUp() {
        userStorage = mockk()
        passwordProcessor = mockk(relaxed = true)
        userSignUpService = UserSignUpService(userStorage, passwordProcessor)
    }

    @Test
    fun `회원가입 성공`() {
        // given
        val userData = UserData(
            email = "test@example.com",
            username = "testuser",
            password = "password123",
            name = "테스트 사용자",
        )

        every { userStorage.existsByEmail(userData.email) } returns false
        every { userStorage.existsByUsername(userData.username) } returns false
        every { userStorage.save(any()) } returns mockk {
            every { id } returns 1L
            every { email } returns userData.email
            every { username } returns userData.username
            every { encodedPassword } returns "encodedPassword"
            every { name } returns userData.name
        }

        // when
        val result = userSignUpService.signUp(userData)

        // then
        assertEquals(userData.email, result.email)
        assertEquals(userData.username, result.username)
        assertEquals(userData.name, result.name)

        verify { userStorage.existsByEmail(userData.email) }
        verify { userStorage.existsByUsername(userData.username) }
        verify { userStorage.save(any()) }
    }

    @Test
    fun `이메일 중복 시 예외 발생`() {
        // given
        val userData = UserData(
            email = "existing@example.com",
            username = "testuser",
            password = "password123",
            name = "테스트 사용자",
        )

        every { userStorage.existsByEmail(userData.email) } returns true

        // when & then
        val exception = assertThrows<CoreException> {
            userSignUpService.signUp(userData)
        }

        assertEquals("이미 존재하는 이메일입니다.", exception.message)
        verify { userStorage.existsByEmail(userData.email) }
        verify(exactly = 0) { userStorage.existsByUsername(any()) }
        verify(exactly = 0) { userStorage.save(any()) }
    }

    @Test
    fun `사용자명 중복 시 예외 발생`() {
        // given
        val userData = UserData(
            email = "test@example.com",
            username = "existinguser",
            password = "password123",
            name = "테스트 사용자",
        )

        every { userStorage.existsByEmail(userData.email) } returns false
        every { userStorage.existsByUsername(userData.username) } returns true

        // when & then
        val exception = assertThrows<CoreException> {
            userSignUpService.signUp(userData)
        }

        assertEquals("이미 존재하는 사용자명입니다.", exception.message)
        verify { userStorage.existsByEmail(userData.email) }
        verify { userStorage.existsByUsername(userData.username) }
        verify(exactly = 0) { userStorage.save(any()) }
    }
}
