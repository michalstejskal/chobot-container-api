package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.bo.*
import cz.chobot.container_api.enum.ModuleOperation
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.repository.ModuleVersionRepository
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.IUserService
import cz.chobot.container_api.service.impl.ModuleService
import cz.chobot.container_api.service.impl.NetworkService
import org.json.XMLTokener.entity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.client.RestTemplate
import java.lang.reflect.UndeclaredThrowableException
import java.net.URI


@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ModuleServiceTest {

    @Mock
    private val restTemplate: RestTemplate? = null

    @Autowired
    private lateinit var userService: IUserService

    @Autowired
    private lateinit var networkService: NetworkService

    @Autowired
    private lateinit var networkTyRepository: NetworkTypeRepository

    @Autowired
    private lateinit var moduleService: ModuleService

    @Autowired
    private lateinit var moduleVersionRepository: ModuleVersionRepository

    @Value("\${image.buider.uri}")
    private val imageBuildUri: String? = null

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
        moduleVersion = ModuleVersion(0L, "v1","", module)
//        moduleVersion = moduleVersionRepository.save(moduleVersion)
        module = Module(0L, 1, "", "", "", mutableSetOf(), moduleVersion,1, "", "", "", "", 0, "", "", "", network, "")

    }

    @Test
    fun badModuleNameTest() {
        exceptionRule.expect(UndeclaredThrowableException::class.java)
        module.name = "some_name"
        moduleService.createModule(module, network, user)
    }


    @Test
    fun emptyModuleNameTest() {
        exceptionRule.expect(UndeclaredThrowableException::class.java)
        module.name = ""
        moduleService.createModule(module, network, user)
    }

    @Test
    fun createModuleTest() {
        Mockito.`when`(restTemplate?.exchange(URI(""), ModuleOperation.CREATE.operation, null, Void::class.java))
                .thenReturn(ResponseEntity( HttpStatus.ACCEPTED))

        module.name = "some-module"
        module = moduleService.createModule(module, network, user)
        assert(module.name == "some-module")
    }


}