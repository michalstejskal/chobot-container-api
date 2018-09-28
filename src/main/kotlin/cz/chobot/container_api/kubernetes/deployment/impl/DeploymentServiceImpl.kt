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
    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, network: Network): ExtensionsV1beta1Deployment? {
        val deploymentName = user.login + "-" + network.name
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val logger = LoggerFactory.getLogger(DeploymentServiceImpl::class.java)

    override fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, module: Module): String{
        val deploymentName = "${user.login}-${module.name}-${module.actualVersion.name}"
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName, module)

        try {
            val deployment = api.createNamespacedDeployment(namespace.metadata.name, newDeployment, "true")
            logger.info("Deployment {} created.", newDeployment.metadata.name)
            return deploymentName


        } catch (exception: ApiException) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.responseBody)
        }

        return ""
    }

    private fun createDeploymentMetadata(deploymentName: String): V1ObjectMeta{
        val meta = V1ObjectMeta()
        meta.name = deploymentName
        return meta
    }

    private fun createDeploymentSpec(deploymentName: String, module: Module): ExtensionsV1beta1DeploymentSpec{
        val spec = ExtensionsV1beta1DeploymentSpec()
        spec.replicas = 1
        spec.strategy = ExtensionsV1beta1DeploymentStrategy()
        spec.strategy.type = "RollingUpdate"
        spec.template = createDeploymentTemplate(deploymentName, module)

        return spec
    }

    private fun createDeploymentTemplate(deploymentName: String, module: Module): V1PodTemplateSpec{
        val template = V1PodTemplateSpec()
        template.metadata = V1ObjectMeta()
        template.metadata.labels = HashMap()
        template.metadata.labels["app"] = deploymentName

        template.spec = V1PodSpec()
        template.spec.containers = createTemplateContainers(deploymentName, module)
        return template
    }


//    vemu sit a udelam deploy nad ni a i nad modulama
    private fun createTemplateContainers(deploymentName: String, module: Module): List<V1Container>{
        val container = V1Container()
        container.name = deploymentName
//        container.image = "datawire/qotm:1.1"
//        container.image = "michal:1"
//        container.image = module.imageId
//        container.image = "localhost:5000/stejskys-handlebrouk-test"
        container.image = module.imageId

        val port = V1ContainerPort()
        port.name = "http-api"
        port.containerPort = module.connectionPort
        container.ports = Arrays.asList(port)

        val env = V1EnvVar()
        env.name = "git_param"
        env.value="nejake git repo"
        container.env = Arrays.asList(env)

        val resource = V1ResourceRequirements()
        resource.limits = HashMap()
        resource.limits["cpu"] = Quantity("0.1")
        resource.limits["memory"] = Quantity("100Mi")
        container.resources = resource




//        val container1 = V1Container()
//        container1.name = "preproces-module"
//        container1.image = "michalsec:1"
//
//        val port1 = V1ContainerPort()
//        port1.name = "http-api"
//        port1.containerPort = 5001
//        container1.ports = Arrays.asList(port1)
//
//        val resource1 = V1ResourceRequirements()
//        resource1.limits = HashMap()
//        resource1.limits["cpu"] = Quantity("0.1")
//        resource1.limits["memory"] = Quantity("100Mi")
//        container1.resources = resource1


//        return Arrays.asList(container, container1)
        return Arrays.asList(container)
    }
}