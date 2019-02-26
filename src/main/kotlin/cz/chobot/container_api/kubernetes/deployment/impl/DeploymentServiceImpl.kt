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
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap


@Service
class DeploymentServiceImpl : IDeploymentService {

    @Value("\${train.data.path}")
    private val filePath: String? = null

    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, network: Network): String {
        val deploymentName = "${user.login}-${network.name}"
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"

        var train_data_path = ""
        val params = network.parameters.filter { parameter -> parameter.name == "TRAIN_DATA_PATH" }
        if (params.size > 0) {
            train_data_path = params.get(0).value
        }

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName, network.type.imageId, 5000, network.id.toString(), train_data_path)

        try {
            val deployment = api.createNamespacedDeployment(namespace.metadata.name, newDeployment, true, "true", "true")
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

    override fun deleteDeployment(api: ExtensionsV1beta1Api, namespace: V1Namespace, deploymentName: String) {
        try {

            val options = V1DeleteOptions()
            options.propagationPolicy = "Foreground"
            api.deleteNamespacedDeployment(deploymentName, namespace.metadata.name, options, "true", "true", 10, true, "Foreground")
        } catch (e: Exception) {
            logger.warn("catch IllegalStateException, bug in kubernetes, continue..")
        }
    }

    private val logger = LoggerFactory.getLogger(DeploymentServiceImpl::class.java)

    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, module: Module): String {
        val deploymentName = "${user.login}-${module.name}-${module.actualVersion.name}"
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"
        var train_data_path = ""

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName, module.imageId, module.connectionPort, module.id.toString(), train_data_path)

        try {
            val deployment = api.createNamespacedDeployment(namespace.metadata.name, newDeployment, true, "true", "true")
            logger.info("Deployment {} created.", newDeployment.metadata.name)
            return deploymentName


        } catch (exception: ApiException) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.responseBody)
        }

        return ""
    }

    private fun createDeploymentMetadata(deploymentName: String): V1ObjectMeta {
        val meta = V1ObjectMeta()
        meta.name = deploymentName
        return meta
    }

    private fun createDeploymentSpec(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, train_data_path: String): ExtensionsV1beta1DeploymentSpec {
        val spec = ExtensionsV1beta1DeploymentSpec()
        spec.replicas = 1
        spec.strategy = ExtensionsV1beta1DeploymentStrategy()
        spec.strategy.type = "RollingUpdate"
        spec.template = createDeploymentTemplate(deploymentName, imageId, connectionPort, networkId, train_data_path)

        return spec
    }

    private fun createDeploymentTemplate(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, train_data_path: String): V1PodTemplateSpec {
        val template = V1PodTemplateSpec()
        template.metadata = V1ObjectMeta()
        template.metadata.labels = HashMap()
        template.metadata.labels["app"] = deploymentName
        template.metadata.labels["id"] = deploymentName


        template.spec = V1PodSpec()
        template.spec.containers = createTemplateContainers(deploymentName, imageId, connectionPort, networkId, train_data_path)

        if (train_data_path.isNotEmpty()) {
            val hostPathVolume = V1HostPathVolumeSource()
            hostPathVolume.path = train_data_path

            val hostVolume = V1Volume()
            hostVolume.name = "mount-volume"
            hostVolume.hostPath = hostPathVolume

            template.spec.volumes = Arrays.asList(hostVolume)
        }
        return template
    }


    //    vemu sit a udelam deploy nad ni a i nad modulama
    private fun createTemplateContainers(deploymentName: String, imageId: String, connectionPort: Int, networkId: String, train_data_path: String): List<V1Container> {
        val container = V1Container()
        container.name = deploymentName
        container.image = imageId

        val port = V1ContainerPort()
        port.name = "http-api"
        port.containerPort = connectionPort
        container.ports = Arrays.asList(port)

        val networkIdEnv = V1EnvVar()
        networkIdEnv.name = "NETWORK_ID"
        networkIdEnv.value = networkId

        val environmentEnv = V1EnvVar()
        environmentEnv.name = "ENVIRONMENT"
        environmentEnv.value = "PRODUCTION"

        container.env = Arrays.asList(environmentEnv, networkIdEnv)


        if (train_data_path.isNotEmpty()) {
            val volume = V1VolumeMount()
            volume.mountPath = train_data_path
            volume.name = "mount-volume"
            container.volumeMounts = Arrays.asList(volume)
        }

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