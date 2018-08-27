package cz.chobot.container_api.kubernetes.deployment.impl

import cz.chobot.container_api.bo.user.User
import cz.chobot.container_api.bo.userApplication.Application
import cz.chobot.container_api.kubernetes.deployment.IDeploymentService
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.models.*
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap


class DeploymentServiceImpl : IDeploymentService {

    private val logger = LoggerFactory.getLogger(DeploymentServiceImpl::class.java)

    public fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, application: Application): ExtensionsV1beta1Deployment?{
        val deploymentName = user.login + "-" + application.name
        val newDeployment = ExtensionsV1beta1Deployment()
        newDeployment.apiVersion = "extensions/v1beta1"
        newDeployment.kind = "Deployment"

        newDeployment.metadata = createDeploymentMetadata(deploymentName)
        newDeployment.spec = createDeploymentSpec(deploymentName)

        try {
            val deployment = api.createNamespacedDeployment(namespace.metadata.name, newDeployment, "true")
            logger.info("Deployment {} created.", newDeployment.metadata.name)
            return deployment

        } catch (exception: ApiException) {
            logger.info("Error occurred while creating deployment {}. Error is: {}", newDeployment.metadata.name, exception.responseBody)
        }

        return null
    }

    private fun createDeploymentMetadata(deploymentName: String): V1ObjectMeta{
        val meta = V1ObjectMeta()
        meta.name = deploymentName
        return meta
    }

    private fun createDeploymentSpec(deploymentName: String): ExtensionsV1beta1DeploymentSpec{
        val spec = ExtensionsV1beta1DeploymentSpec()
        spec.replicas = 1
        spec.strategy = ExtensionsV1beta1DeploymentStrategy()
        spec.strategy.type = "RollingUpdate"
        spec.template = createDeploymentTemplate(deploymentName)

        return spec
    }

    private fun createDeploymentTemplate(deploymentName: String): V1PodTemplateSpec{
        val template = V1PodTemplateSpec()
        template.metadata = V1ObjectMeta()
        template.metadata.labels = HashMap()
        template.metadata.labels["app"] = deploymentName

        template.spec = V1PodSpec()
        template.spec.containers = createTemplateContainers(deploymentName)
        return template
    }

    private fun createTemplateContainers(deploymentName: String): List<V1Container>{
        val container = V1Container()
        container.name = deploymentName
//        container.image = "datawire/qotm:1.1"
        container.image = "michal:1"

        val port = V1ContainerPort()
        port.name = "http-api"
        port.containerPort = 5000
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




        val container1 = V1Container()
        container1.name = "preproces-module"
        container1.image = "michalsec:1"

        val port1 = V1ContainerPort()
        port1.name = "http-api"
        port1.containerPort = 5001
        container1.ports = Arrays.asList(port1)

        val resource1 = V1ResourceRequirements()
        resource1.limits = HashMap()
        resource1.limits["cpu"] = Quantity("0.1")
        resource1.limits["memory"] = Quantity("100Mi")
        container1.resources = resource1


        return Arrays.asList(container, container1)
    }
}