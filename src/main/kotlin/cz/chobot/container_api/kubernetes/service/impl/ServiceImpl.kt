package cz.chobot.container_api.kubernetes.service.impl

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.kubernetes.service.Iservice
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.models.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*


@Service
class ServiceImpl : Iservice {

    private val logger = LoggerFactory.getLogger(ServiceImpl::class.java)

    /***
     * Create new service for network
     */
    override fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, network: Network): V1Service? {
        val serviceName = "${user.login}-${network.name}"
        val newService = V1Service()
        newService.apiVersion = "v1"
        newService.kind = "Service"
        newService.metadata = createServiceMetadata(serviceName)
        newService.spec = createServiceSpec(serviceName, 80, "http-" + serviceName, "http-api")
        try {
            val service = api.createNamespacedService(namespace.metadata.name, newService, true, "true", "true")
            logger.info("Service ${newService.metadata.name} created")
            return service
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating service ${serviceName}. Error is: ${exception.message}")
            logger.error(exception.responseBody)
        } catch (exception: Exception) {
            logger.info("Error occurred while creating service ${serviceName}. Error is: ${exception.message}")
            exception.printStackTrace()
        }

        return newService
    }

    /***
     * Create new service for module
     */
    override fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, module: Module): V1Service? {
        val serviceName = "${user.login}-${module.name}-${module.actualVersion.name}"
        val newService = V1Service()
        newService.apiVersion = "v1"
        newService.kind = "Service"
        newService.metadata = createServiceMetadata(serviceName)
        newService.spec = createServiceSpec(serviceName, 80, "http-" + serviceName, "http-api")
        try {
            val service = api.createNamespacedService(namespace.metadata.name, newService, true, "true", "true")
            logger.info("Service {} created", newService.metadata.name)
            return service
        } catch (exception: ApiException) {
            logger.error(exception.responseBody)
        }

        return newService
    }

    /***
     * Delete service for network/module by its selector
     */
    override fun deleteService(api: CoreV1Api, namespace: V1Namespace, serviceName: String) {
        val body = V1DeleteOptions()
        api.deleteNamespacedService(serviceName, namespace.metadata.name, body, "true", "true", 15, true, "true")
    }

    /***
     * Set selector for new service, define its port
     */
    private fun createServiceSpec(selectorApp: String, servicePort: Int, servicePortName: String, targetPort: String): V1ServiceSpec {
        val spec = V1ServiceSpec()
        spec.selector = HashMap()
        spec.selector["app"] = selectorApp
        spec.selector["id"] = selectorApp

        val port = V1ServicePort()
        port.port = servicePort
        port.name = servicePortName
        port.targetPort = IntOrString(targetPort)
        spec.ports = Arrays.asList(port)
        return spec
    }

    /***
     * set meta info for service -- only name and Ambasador annotations
     */
    private fun createServiceMetadata(serviceName: String): V1ObjectMeta {
        val meta = V1ObjectMeta()
        meta.name = serviceName
        meta.annotations = createAnnotations(serviceName)

        return meta
    }


    /***
     * create and set annotations for Kubernetes ambasador
     */
    private fun createAnnotations(serviceName: String): HashMap<String, String> {
        val annotations = HashMap<String, String>()
        annotations.put("getambassador.io/config", "---\n" +
                "apiVersion: ambassador/v0\n" +
                "kind:  Mapping\n" +
                "name:  mapping-$serviceName\n" +
                "prefix: /$serviceName/\n" +
                "service: $serviceName\n" +
                "timeout_ms: " + 30000)

        return annotations
    }
}