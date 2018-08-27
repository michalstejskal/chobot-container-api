package cz.chobot.container_api.kubernetes.pod.impl

import cz.chobot.container_api.kubernetes.pod.IPodService
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.models.*
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap

class PodService : IPodService {
    private val logger = LoggerFactory.getLogger(PodService::class.java)

    override fun createPod(api: CoreV1Api, namespace: V1Namespace): V1Pod? {
        try {
            val pod = createPod(namespace)
            val namespacedPod: V1Pod = api.createNamespacedPod(pod.metadata.namespace, pod, "true")
            logger.info("Pod successfully created.")
            return namespacedPod
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating pod. Error is: {}", exception.responseBody)
        }

        return null
    }

    override fun createService(api: CoreV1Api, namespace: V1Namespace): V1Service? {
        try {
            val service = createService(namespace)
            val namespacedService: V1Service = api.createNamespacedService(service.metadata.namespace, service, "true")
            logger.info("Service successfully created.")
            return namespacedService
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating service. Error is: {}", exception.responseBody)
        }

        return null
    }

    fun createReplicationController(api: CoreV1Api, namespace: V1Namespace): V1ReplicationController{
        val controller = V1ReplicationController()
        controller.kind = "ReplicationController"

        val controllerMeta = V1ObjectMeta()
        controllerMeta.name = "mock-user-rc"
        controller.metadata = controllerMeta


        val spec = V1ReplicationControllerSpec()
        spec.replicas = 1


        val template = V1PodTemplateSpec()

        val templateMeta = V1ObjectMeta()
        templateMeta.labels = HashMap()
        templateMeta.labels["app"] ="mock-user-app"
        template.metadata = templateMeta


        val templateSpec = V1PodSpec()

        val templateSpecContainer = V1Container()
        templateSpecContainer.name = "mock-user-api"
        templateSpecContainer.image = "chobot_labs:mock_user_1"
        val port = V1ContainerPort()
        port.containerPort(5000)
        templateSpecContainer.ports = Arrays.asList(port)


        val templateSpecContainerLivenesProbe =  V1Probe()


        val httpGetAction = V1HTTPGetAction()
        httpGetAction.path = "/"
        httpGetAction.port = IntOrString(5000)

        templateSpecContainerLivenesProbe.httpGet = httpGetAction

        templateSpecContainerLivenesProbe.initialDelaySeconds = 30
        templateSpecContainerLivenesProbe.timeoutSeconds = 10
        templateSpecContainer.livenessProbe = templateSpecContainerLivenesProbe



        templateSpec.containers = Arrays.asList(templateSpecContainer)
        template.spec = templateSpec


        spec.template = template
        controller.spec = spec


        val newController = api.createNamespacedReplicationController(namespace.metadata.name, controller, "true")
        return newController

    }


    override fun findPod(api: CoreV1Api, namespace: V1Namespace, podName: String): V1Pod? {
        try {
            return api.readNamespacedPod(podName, namespace.metadata?.name, "true", true, false)
        }catch (exception: ApiException){
            logger.error("Error occurred while creating pod. Error is: {}", exception.responseBody)
        }

        return null
    }


    private fun createPod(namespace: V1Namespace): V1Pod {
        logger.info("Creating new pod.")

        val pod = V1Pod()
        pod.apiVersion = "v1"
        pod.kind = "Pod"
        pod.metadata = createPodMetadata(namespace)
        pod.spec = createPodSpec()

        return pod

    }


    private fun createService(namespace: V1Namespace): V1Service {
        logger.info("Creating new pod.")

        val service = V1Service()
        service.apiVersion = "v1"
        service.kind = "Service"
        service.metadata = createServiceMetadata(namespace)
        service.spec = createServiceSpec()

        return service

    }



    private fun createPodMetadata(namespace: V1Namespace): V1ObjectMeta {
        val metadata = V1ObjectMeta()
        metadata.name = "mock-user-pod"
        metadata.labels = HashMap()
        metadata.labels["app"] = "mock-user-app"
        metadata.namespace = namespace.metadata.name
        metadata.clusterName

        return metadata
    }

    private fun createServiceMetadata(namespace: V1Namespace): V1ObjectMeta {
        val metadata = V1ObjectMeta()
        metadata.name = "mock-user-service"
        metadata.labels = HashMap()
        metadata.labels["app"] = "mock-user-app"
        metadata.namespace = namespace.metadata.name
        metadata.clusterName

        return metadata
    }

    private fun createServiceSpec(): V1ServiceSpec{
        val spec = V1ServiceSpec()
        spec.type = "NodePort"
        spec.selector =HashMap()
        spec.selector["app"] = "mock-user-app"

        val port = V1ServicePort()
        port.protocol = "TCP"
        port.port = 5000
        port.name = "http"
        spec.ports = Arrays.asList(port)

        return spec
    }

    private fun createPodSpec(): V1PodSpec {
        val spec = V1PodSpec()
        spec.containers = ArrayList()


        val container = V1Container()
        container.name = "mock-user-api"
        container.image = "chobot_labs:mock_user_1"
        val port = V1ContainerPort()
        port.containerPort(5000)
        container.ports = Arrays.asList(port)

        val readinessHttpGet = V1HTTPGetAction()
        readinessHttpGet.path = "/"
        readinessHttpGet.port = IntOrString(5000)

        val readinessProbe = V1Probe()
        readinessProbe.httpGet = readinessHttpGet
        container.readinessProbe = readinessProbe

        spec.containers.add(container)

        return spec
    }


    override fun deletePod(api: CoreV1Api, namespace: V1Namespace, pod: V1Pod) {
        try {
            logger.info("Deleting pod {} in namespace {}.", pod.metadata.name, namespace.metadata.name)
            val body = V1DeleteOptions() // V1DeleteOptions
            val gracePeriodSeconds = 0 // Integer | The duration in seconds before the object should be deleted. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period for the specified type will be used. Defaults to a per object value if not specified. zero means delete immediately.
            val orphanDependents = true
            val propagationPolicy = "Orphan" // String | Whether and how garbage collection will be performed. Either this field or OrphanDependents may be set, but not both. The default policy is decided by the existing finalizer set in the metadata.finalizers and the resource-specific default policy. Acceptable values are: 'Orphan' - orphan the dependents; 'Background' - allow the garbage collector to delete the dependents in the background; 'Foreground' - a cascading policy that deletes all dependents in the foreground.

            val status = api.deleteNamespacedPod(pod.metadata.name, pod.metadata.namespace, body, "true", gracePeriodSeconds, null, propagationPolicy)
            logger.error("Pod deleted with status: {}.", status)
        }catch (exception: ApiException){
            logger.error("Error occurred while deleting pod {}. Error is", pod.metadata.name, exception.responseBody)
        }catch (exception: Exception){
            logger.error("Error occurred while deleting pod {}. Error is", pod.metadata.name, exception.message)
        }

    }

    override fun listPods(api: CoreV1Api) {
        val list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null)
        println("All pods info BEGIN")
        for (item in list.items) {
            println(item.metadata.name)
        }
        println("All pods info END")

    }

    override fun listServices(api: CoreV1Api){
        var list = api.listServiceForAllNamespaces(null,null,null,null,null,null,null,null,null)
        println("All services info BEGIN")
        for (item in list.items) {
            println(item.metadata.name)
        }
        println("All services info END")
    }
}