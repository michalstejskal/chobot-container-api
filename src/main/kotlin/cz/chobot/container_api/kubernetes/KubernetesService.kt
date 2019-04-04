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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class KubernetesService : IKubernetesService {
    private val logger = LoggerFactory.getLogger(KubernetesService::class.java)

    @Autowired
    private lateinit var namespaceService: INamespaceService

    @Autowired
    private lateinit var service: ServiceImpl

    @Autowired
    private lateinit var deploymentService: IDeploymentService

    @Value("\${ambasador.service.url}")
    private val ambasadorServiceUrl: String? = null

    @Value("\${ambasador.service.url.internal}")
    private val ambasadorServiceUrlInternal: String? = null

    /***
     * Wrapper for getPodsLogs for module
     */
    override fun getPodLogs(module: Module, user: User): String {
        val label = "${user.login}-${module.name}-${module.actualVersion.name}"
        return getPodLogs(label)
    }

    /***
     * Wrapper for getPodsLogs for Network
     */
    override fun getPodLogs(network: Network, user: User): String {
        val label = "${user.login}-${network.name}"
        return getPodLogs(label)
    }

    /***
     * Create new deployments and service for network
     */
    override fun deployNetwork(network: Network, user: User): Network {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        service.createService(api, namespace, user, network)
        val deploymentName = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, network)
        network.connectionUri = "$ambasadorServiceUrl/$deploymentName/"
        network.status = 4
        return network
    }

    /***
     * Create new deployments and service for module
     */
    override fun deployModule(module: Module, user: User): Module {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        service.createService(api, namespace, user, module)
        val deploymentName = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, module)
        module.connectionUri = "$ambasadorServiceUrl/$deploymentName/"
        module.connectionUriInternal = "$ambasadorServiceUrlInternal/$deploymentName/"
        return module
    }


    /***
     * Delete deployments and service for netowrk
     */
    override fun undeployNetwork(network: Network, user: User) {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        service.deleteService(api, namespace, "${user.login}-${network.name}")
        logger.info("Service for ${user.login}-${network.name} deleted")
        deploymentService.deleteDeployment(ExtensionsV1beta1Api(), namespace, "${user.login}-${network.name}")
        logger.info("Deployment for ${user.login}-${network.name} deleted")
    }

    /***
     * Delete deployments and service for module
     */
    override fun undeployModule(module: Module, user: User) {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        service.deleteService(api, namespace, "${user.login}-${module.name}-${module.actualVersion.name}")
        logger.info("Service for ${user.login}-${module.name}-${module.actualVersion.name} deleted")
        deploymentService.deleteDeployment(ExtensionsV1beta1Api(), namespace, "${user.login}-${module.name}-${module.actualVersion.name}")
        logger.info("Deployment for ${user.login}-${module.name}-${module.actualVersion.name} deleted")
    }

    /***
     * Return Kube api for creating service and namespaces
     */
    private fun getKubernetesApi(): CoreV1Api {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        val api = CoreV1Api()
        return api
    }

    // return only some logs
    // all in elastic search
    /***
     * Get Pod logs by its selector
     */
    private fun getPodLogs(label: String): String {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        try {
            val pods = api.listPodForAllNamespaces(null, null, false, "app=$label", 10, "true", null, 10, false)
            if (pods != null && pods.items != null && !pods.items.isEmpty()) {

                api.readNamespacedPod(pods.items[0].metadata.name, namespace.metadata.name, "true", false, false)
                val logs = api.readNamespacedPodLog(pods.items[0].metadata.name, namespace.metadata.name, null, null, null, null, null, null, 50, true)
                return logs
            }

        } catch (e: Exception) {
            // pod does not logged or it's not existing pod
            logger.error("GET LOGS return: ${e.localizedMessage}")
            //e.printStackTrace()
        }

        return ""
    }


}