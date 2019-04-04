package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkType
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import cz.chobot.container_api.kubernetes.namespace.INamespaceService
import cz.chobot.container_api.kubernetes.service.Iservice
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.util.Config
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class KubeServiceTest {

    @Autowired
    private lateinit var serviceService: Iservice

    @Autowired
    private lateinit var namespaceService: INamespaceService

    private lateinit var user: User
    private lateinit var network: Network
    private lateinit var namespace: V1Namespace
    private lateinit var api: CoreV1Api

    @Before
    fun createUserAndNetwork() {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        api = CoreV1Api()

        user = User(0L, "user", "", "", "", "", mutableSetOf())
        network = Network(0L, NetworkType(0L, "some-network-type", "", "", ""), "network", "", "", "", 1, "", "", "", "", mutableSetOf(), mutableSetOf(), user);
        namespace = namespaceService.getOrCreateNamespace(api, "default")
    }

    @After
    fun cleanKubeCluster() {
        serviceService.deleteService(api, namespace, "${user.login}-${network.name}")
    }

    @Test
    fun createDeploymentTest() {
        serviceService.createService(api, namespace, user, network)
    }


}
