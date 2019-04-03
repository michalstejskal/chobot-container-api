package cz.chobot.container_api.servicesTest

import cz.chobot.container_api.ContainerApiApplication
import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkType
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.service.IFileService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.io.FileInputStream
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import org.junit.rules.ExpectedException




//@RunWith(MockitoJUnitRunner::class)
@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileServiceTest {

    @Autowired
    private lateinit var fileService: IFileService

    private lateinit var network: Network
    private lateinit var user: User

    @get:Rule
    public var exceptionRule = ExpectedException.none()


    @Before
    fun createUserAndNetwork() {
        user = User(0L, "","","","","", mutableSetOf())
        network = Network(0L, NetworkType(0L, "some-network-type", "", "", ""), "", "", "", "", 1, "", "", "", "", mutableSetOf(), mutableSetOf(), user);
    }

    @Test
    fun emptyFileTest() {
        exceptionRule.expect(ControllerException::class.java)
        exceptionRule.expectMessage("ER005 - FILE IS EMPTY")
        val multipartFile = MockMultipartFile("user-file", "test.txt", null, "".toByteArray())
        val saveFileToPath = fileService.saveFileToPath(multipartFile, network, user)
    }


}