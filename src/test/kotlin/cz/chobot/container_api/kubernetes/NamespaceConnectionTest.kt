package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.kubernetes.namespace.INamespaceService
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.util.Config
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class NamespaceConnectionTest {

    @Autowired
    private lateinit var namespaceService: INamespaceService

    private lateinit var api: CoreV1Api

    @Before
    fun getKubernetesApi() {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        api = CoreV1Api()
    }

    @Test
    fun testDefaultNamespace() {
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        assert(namespace.apiVersion == "v1")
        assert(namespace.metadata.name == "default")
    }

}
