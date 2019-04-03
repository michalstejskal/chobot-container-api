package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.bo.*
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.IUserService
import cz.chobot.container_api.service.impl.ModuleService
import cz.chobot.container_api.service.impl.NetworkService
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
class ModuleServiceTest {

    @Autowired
    private lateinit var userService: IUserService

    @Autowired
    private lateinit var networkService: NetworkService

    @Autowired
    private lateinit var networkTyRepository: NetworkTypeRepository

    @Autowired
    private lateinit var moduleService: ModuleService

    private lateinit var network: Network
    private lateinit var user: User
    private lateinit var networkType: NetworkType
    private lateinit var module: Module
    private lateinit var moduleVersion: ModuleVersion

    @get:Rule
    public var exceptionRule = ExpectedException.none()


    @Before
    fun createUserAndNetwork() {
        user = User(0L, "testUser", "", "testFirstName", "testLastName", "testUser@test.com", mutableSetOf())
        user = userService.createUser(user)
        networkType = NetworkType(0L, "some-network-type", "", "", "")
        networkType = networkTyRepository.save(networkType)
        network = Network(0L, networkType, "network", "", "", "", 1, "", "", "", "", mutableSetOf(), mutableSetOf(), user);
        network = networkService.createNetwork(network, user)
        moduleVersion = ModuleVersion(0L, "","",module)
        module = Module(0L, 1, "", "", "", mutableSetOf(), moduleVersion, 1, "", "", "", "", 0, "", "", "", network, "")

    }

    @Test
    fun badModuleNameTest() {
        exceptionRule.expect(ControllerException::class.java)
        exceptionRule.expectMessage("ER009 - BAD MODULE NAME")
        moduleService.createModule(module, network, user)
    }


}