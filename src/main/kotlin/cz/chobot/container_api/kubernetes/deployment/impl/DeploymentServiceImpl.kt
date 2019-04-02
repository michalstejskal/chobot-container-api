package cz.chobot.container_api.kubernetes.deployment.impl

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.models.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap


@Service
class DeploymentServiceImpl : IDeploymentService {

    private val logger = LoggerFactory.getLogger(DeploymentServiceImpl::class.java)

    /***
     * Create deployment for network
     */
    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, network: Network): String {
        val deploymentName = "${user.login}-${network.name}"
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"

        var trainDataPath = ""
        val params = network.parameters.filter { parameter -> parameter.name == "TRAIN_DATA_PATH" }
        if (params.isNotEmpty()) trainDataPath = params[0].value

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName, network.type.imageId, 5000, network.id.toString(), trainDataPath, network.apiKeySecret)

        try {
            api.createNamespacedDeployment(namespace.metadata.name, newDeployment, true, "true", "true")
            logger.info("Deployment {} created.", newDeployment.metadata.name)
            return deploymentName
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.responseBody)
        } catch (exception: Exception) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.message)
            exception.printStackTrace()
        }

        return ""
    }

    /***
     * Create deployment for module
     */
    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, module: Module): String {
        // deployment name
        val deploymentName = "${user.login}-${module.name}-${module.actualVersion.name}"
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"

        // createDeploymentSpec() is also used for networks
        val trainDataPath = ""

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName, module.imageId, module.connectionPort, module.id.toString(), trainDataPath, module.apiKeySecret)

        try {
            api.createNamespacedDeployment(namespace.metadata.name, newDeployment, true, "true", "true")
            logger.info("Deployment {} created.", newDeployment.metadata.name)
            return deploymentName
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.responseBody)
        }
        return ""
    }

    /***
     * Delete deployment -- match by it's selector. Wait 10 seconds to stop application, otherwise kill
     */
    override fun deleteDeployment(api: ExtensionsV1beta1Api, namespace: V1Namespace, deploymentName: String) {
        try {
            val options = V1DeleteOptions()
            options.propagationPolicy = "Foreground"
            api.deleteNamespacedDeployment(deploymentName, namespace.metadata.name, options, "true", "true", 10, true, "Foreground")
        } catch (e: Exception) {
            logger.warn("catch IllegalStateException, bug in kubernetes, continue..")
        }
    }

    /**
     * Create deployment meta info -- sets only name
     */
    private fun createDeploymentMetadata(deploymentName: String): V1ObjectMeta {
        val meta = V1ObjectMeta()
        meta.name = deploymentName
        return meta
    }

    /***
     * Create spec for deployment, set num of replica for each Pod
     */
    private fun createDeploymentSpec(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, trainDataPath: String, secret: String): ExtensionsV1beta1DeploymentSpec {
        val spec = ExtensionsV1beta1DeploymentSpec()
        spec.replicas = 1
        spec.strategy = ExtensionsV1beta1DeploymentStrategy()
        spec.strategy.type = "RollingUpdate"
        spec.template = createDeploymentTemplate(deploymentName, imageId, connectionPort, networkId, trainDataPath, secret)

        return spec
    }

    /***
     * Create PodTemplate and set its selector and train data Volume
     */
    private fun createDeploymentTemplate(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, trainDataPath: String, secret: String): V1PodTemplateSpec {

        // selector for pods and deployment -- matched with service by this
        val template = V1PodTemplateSpec()
        template.metadata = V1ObjectMeta()
        template.metadata.labels = HashMap()
        template.metadata.labels["app"] = deploymentName
        template.metadata.labels["id"] = deploymentName


        template.spec = V1PodSpec()
        template.spec.containers = createTemplateContainers(deploymentName, imageId, connectionPort, networkId, trainDataPath, secret)

        // if train data is not empty mount it's location to Pod
        if (trainDataPath.isNotEmpty()) {
            val hostPathVolume = V1HostPathVolumeSource()
            hostPathVolume.path = trainDataPath

            val hostVolume = V1Volume()
            hostVolume.name = "mount-volume"
            hostVolume.hostPath = hostPathVolume

            template.spec.volumes = Arrays.asList(hostVolume)
        }
        return template
    }

    /***
     * Set containers for Pods created with this deployments, set environment properties and limits on container
     */
    private fun createTemplateContainers(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, trainDataPath: String, secret: String): List<V1Container> {
        val container = V1Container()
        container.name = deploymentName
        container.image = imageId

        val port = V1ContainerPort()
        port.name = "http-api"
        port.containerPort = connectionPort

        // url for swagger ui
        val portSwagger = V1ContainerPort()
        portSwagger.name = "api-swagger"
        portSwagger.containerPort = 8080
        container.ports = Arrays.asList(port, portSwagger)

        // network id. Its used when network or module start to get their own identity
        val networkIdEnv = V1EnvVar()
        networkIdEnv.name = "NETWORK_ID"
        networkIdEnv.value = networkId

        // if run in DEVEL or PROD -- always in PROD
        val environmentEnv = V1EnvVar()
        environmentEnv.name = "ENVIRONMENT"
        environmentEnv.value = "PRODUCTION"

        // URI for API -- used by swagger
        val uriEnv = V1EnvVar()
        uriEnv.name = "API_URI"
        uriEnv.value = "/$deploymentName/"

        // secret for modules, because they don't have access to DB
        val secrtetEnv = V1EnvVar()
        secrtetEnv.name = "API_SECRET"
        secrtetEnv.value = secret

        container.env = Arrays.asList(environmentEnv, networkIdEnv, uriEnv, secrtetEnv)

        // if network and has training data set their location
        if (trainDataPath.isNotEmpty()) {
            val volume = V1VolumeMount()
            volume.mountPath = trainDataPath
            volume.name = "mount-volume"
            container.volumeMounts = Arrays.asList(volume)
        }

        // limits on containers
        val resource = V1ResourceRequirements()
        resource.limits = HashMap()
        resource.limits["cpu"] = Quantity("0.1")
        resource.limits["memory"] = Quantity("1Gi")
        resource.requests = HashMap()
        resource.requests["memory"] = Quantity("1Gi")
        container.resources = resource
        return Arrays.asList(container)
    }
}