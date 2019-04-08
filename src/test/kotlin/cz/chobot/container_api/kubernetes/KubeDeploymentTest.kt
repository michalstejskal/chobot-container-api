package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkType
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import cz.chobot.container_api.kubernetes.namespace.INamespaceService
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
class KubeDeploymentTest {

    @Autowired
    private lateinit var deploymentService: IDeploymentService

    @Autowired
    private lateinit var namespaceService: INamespaceService

    private lateinit var user: User
    private lateinit var network: Network
    private lateinit var namespace: V1Namespace


    @Before
    fun createUserAndNetwork() {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        val api = CoreV1Api()

        user = User(0L, "user", "", "", "", "", mutableSetOf())
        network = Network(0L, NetworkType(0L, "some-network-type", "localhost:5000/chobot_chatbot", "", "", 0), "network", "", "", "", 1, "", "", "", "", mutableSetOf(), mutableSetOf(), user);
        namespace = namespaceService.getOrCreateNamespace(api, "default")
    }

    @After
    fun cleanKubeCluster() {
        deploymentService.deleteDeployment(ExtensionsV1beta1Api(), namespace, "${user.login}-${network.name}")
    }

    @Test
    fun createDeploymentTest() {
        deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, network)
    }

}
