package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import cz.chobot.container_api.kubernetes.namespace.INamespaceService
import cz.chobot.container_api.kubernetes.service.impl.ServiceImpl
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.util.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KubernetesService : IKubernetesService {


    @Autowired
    private lateinit var namespaceService: INamespaceService

    @Autowired
    private lateinit var service: ServiceImpl

    @Autowired
    private lateinit var deploymentService: IDeploymentService

    @Value("\${ambasador.service.url}")
    private val ambasadorServiceUrl: String? = null

    override fun deployNetwork(network: Network, user: User): Network {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        val service = service.createService(api, namespace, user, network)
        val deployment = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, network)

        return network
    }


    override fun deployModule(module: Module, user: User): Module {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        val service = service.createService(api, namespace, user, module)
        val deploymentName = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, module)
        module.connectionUri = "$ambasadorServiceUrl/$deploymentName"
        return module
    }

    private fun getKubernetesApi(): CoreV1Api {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        val api = CoreV1Api()
        return api
    }


}