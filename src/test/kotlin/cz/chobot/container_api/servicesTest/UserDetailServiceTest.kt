package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.service.IUserService
import cz.chobot.container_api.service.impl.UserDetailService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.junit.rules.ExpectedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.annotation.DirtiesContext


@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDetailServiceTest {

    @Autowired
    private lateinit var userDetailService: UserDetailService

    @Autowired
    private lateinit var userService: IUserService

    private lateinit var user: User

    @get:Rule
    public var exceptionRule = ExpectedException.none()


    @Before
    fun createUser() {
        user = User(0L, "testUser", "", "testFirstName", "testLastName", "testUser@test.com", mutableSetOf())
        user = userService.createUser(user)
    }

    @Test
    fun notExistingUserNameTest() {
        exceptionRule.expect(UsernameNotFoundException::class.java)
        exceptionRule.expectMessage("some-login")
        userDetailService.loadUserByUsername("some-login")
    }

    @Test
    fun findByUserNameTest() {
        val userFound = userDetailService.loadUserByUsername(user.login)
        assert(userFound.username == user.login)
        assert(userFound.authorities.isEmpty())
    }

}