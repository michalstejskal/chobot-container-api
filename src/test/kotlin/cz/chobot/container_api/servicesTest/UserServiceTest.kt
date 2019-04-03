package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.service.IUserService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.junit.rules.ExpectedException
import org.springframework.test.annotation.DirtiesContext


@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private lateinit var userService: IUserService

    private lateinit var user: User

    @get:Rule
    public var exceptionRule = ExpectedException.none()


    @Before
    fun createUserAndNetwork() {
        user = User(0L, "testUser", "", "testFirstName", "testLastName", "testUser@test.com", mutableSetOf())
    }

    @Test
    fun badUserNameTest() {
        exceptionRule.expect(ControllerException::class.java)
        exceptionRule.expectMessage("ER009 BAD USERNAME")
        user.login = "some_name"
        userService.createUser(user)
    }

    @Test
    fun emptyUserNameTest() {
        exceptionRule.expect(ControllerException::class.java)
        exceptionRule.expectMessage("ER009 BAD USERNAME")
        user.login = ""
        userService.createUser(user)
    }

    @Test
    fun createUserLoginToLowwerCaseTest() {
        user.login = "TESTUSER"
        val createdUser = userService.createUser(user)
        assert(createdUser.login == "testuser")
        assert(createdUser.login != "TESTUSER")
    }


    @Test
    fun createUserTest() {
        val createdUser = userService.createUser(user)
        assert(createdUser.login == user.login)
        assert(createdUser.firstName == user.firstName)
        assert(createdUser.lastName == user.lastName)
        assert(createdUser.email == user.email)
        assert(createdUser.networks.isEmpty())
    }

    @Test
    fun userByLoginTest() {
        val createdUser = userService.createUser(user)
        val findUser = userService.findUser(createdUser.id)
        assert(findUser.isPresent)
        assert(createdUser.equals(findUser.get()))
    }

    @Test
    fun updateUserTest() {
        val createdUser = userService.createUser(user)
        val foundUserOpt = userService.findUser(createdUser.id)

        assert(foundUserOpt.isPresent)
        val foundUser = foundUserOpt.get()
        assert(createdUser.equals(foundUser))

        foundUser.login = "updatedUser"
        userService.updateUser(foundUser)

        val updatedUserOpt = userService.findUser(foundUser.id)
        assert(updatedUserOpt.isPresent)

        val updatedUser = updatedUserOpt.get()
        assert(updatedUser.equals(foundUser))
    }

}