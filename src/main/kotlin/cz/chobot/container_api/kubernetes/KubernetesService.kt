package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import cz.chobot.container_api.kubernetes.namespace.INamespaceService
import cz.chobot.container_api.kubernetes.service.impl.ServiceImpl
import io.kubernetes.client.ApiException
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.models.V1LabelSelector
import io.kubernetes.client.util.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import io.kubernetes.client.ApiClient
import java.io.IOException
import com.google.common.io.ByteStreams
import io.kubernetes.client.PodLogs
import io.kubernetes.client.models.V1Pod
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.io.BufferedReader


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

    override fun getPodLogs(module: Module, user: User):String {
        val label = "${user.login}-${module.name}-${module.actualVersion.name}"
        return getPodLogs(label)
    }

    override fun getPodLogs(network: Network, user: User): String {
        val label = "${user.login}-${network.name}"
        return getPodLogs(label)
    }

    override fun deployNetwork(network: Network, user: User): Network {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        val service = service.createService(api, namespace, user, network)
        val deploymentName = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, network)
        network.connectionUri = "$ambasadorServiceUrl/$deploymentName/"
        network.status = 4
        return network
    }


    override fun deployModule(module: Module, user: User): Module {
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        val service = service.createService(api, namespace, user, module)
        val deploymentName = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, module)
        module.connectionUri = "$ambasadorServiceUrl/$deploymentName/"
        module.connectionUriInternal = "$ambasadorServiceUrlInternal/$deploymentName/"
        return module
    }


    private fun getKubernetesApi(): CoreV1Api {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        val api = CoreV1Api()
        return api
    }

//    return only some logs
//    all in elastic search
    private fun getPodLogs(label: String): String{
        val api = getKubernetesApi()
        val namespace = namespaceService.getOrCreateNamespace(api, "default")
        try {
            val pods = api.listPodForAllNamespaces(null, null, false, "app=$label", 10, "true", null, 10, false)
            if (pods != null && pods.items != null && !pods.items.isEmpty()) {

                val pod = api.readNamespacedPod(pods.items[0].metadata.name, namespace.metadata.name, "true", false, false)
                val logs = api.readNamespacedPodLog(pods.items[0].metadata.name, namespace.metadata.name, null, null, null, null, null, null, 50, true)
                return logs

            }

        } catch (e: Exception) {
            logger.error(e.localizedMessage)
            e.printStackTrace()
        }

        return ""
    }


}