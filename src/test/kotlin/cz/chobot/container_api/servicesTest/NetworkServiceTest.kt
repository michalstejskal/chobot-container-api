package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.NetworkType
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.INetworkService
import cz.chobot.container_api.service.IUserService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.lang.reflect.UndeclaredThrowableException


@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class NetworkServiceTest {


    @Autowired
    private lateinit var userService: IUserService

    @Autowired
    private lateinit var networkService: INetworkService

    @Autowired
    private lateinit var networkTypeRepository: NetworkTypeRepository

    @Autowired
    private lateinit var networkRepository: NetworkRepository

    private lateinit var network: Network
    private lateinit var user: User
    private lateinit var networkType: NetworkType


    @get:Rule
    public var exceptionRule = ExpectedException.none()


    @Before
    fun createUserAndNetwork() {
        user = User(0L, "testUser", "", "testFirstName", "testLastName", "testUser@test.com", mutableSetOf())
        user = userService.createUser(user)
        networkType = NetworkType(0L, "some-network-type", "", "", "",0)
        network = Network(0L, networkType, "network", "", "", "", 1, "", "", "", "", mutableSetOf(), mutableSetOf(), user);
    }

    @Test
    fun invalidNetworkNameTest() {
        exceptionRule.expect(UndeclaredThrowableException::class.java)
        network.name = "network_network"
        networkService.createNetwork(network, user)
    }

    @Test
    fun emptyNetworkNameTest() {
        exceptionRule.expect(UndeclaredThrowableException::class.java)
        network.name = ""
        networkService.createNetwork(network, user)
    }

    @Test
    fun invalidNetworkTypeTest(){
        exceptionRule.expect(UndeclaredThrowableException::class.java)
        networkService.createNetwork(network, user)
    }

    @Test
    fun createNetwork(){
        networkType = networkTypeRepository.save(networkType)
        network.type = networkType
        networkService.createNetwork(network, user)
    }

    @Test
    fun testNetworkParameter(){
        networkType = networkTypeRepository.save(networkType)
        network.type = networkType
        network = networkService.createNetwork(network, user)

        val networkParam = NetworkParameter(0L, "IS_TRAINED",network, "IS_TRAINED", "TRUE")
        network.parameters.add(networkParam)
        networkRepository.save(network)


        val networkOpt = networkRepository.findById(network.id)
        if(networkOpt.isPresent){
            assert(networkOpt.get().parameters.isNotEmpty())
        }else{
            //network not found -- something is wrong
            assert(1==2)
        }

    }

    @Test
    fun resetNetworkParameters(){
        networkType = networkTypeRepository.save(networkType)
        network.type = networkType
        network = networkService.createNetwork(network, user)

        val networkParam = NetworkParameter(0L, "IS_TRAINED",network, "IS_TRAINED", "TRUE")
        network.parameters.add(networkParam)
        network = networkRepository.save(network)

        networkService.resetNetworkAttributes(network)

        val networkOpt = networkRepository.findById(network.id)
        if(networkOpt.isPresent){
            assert(networkOpt.get().parameters.isEmpty())
        }else{
            //network not found -- something is wrong
            assert(1==2)
        }




    }


}