package cz.chobot.container_api.kubernetes.service.impl

import cz.chobot.container_api.bo.user.User
import cz.chobot.container_api.bo.userApplication.Application
import cz.chobot.container_api.kubernetes.service.Iservice
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.models.*
import org.slf4j.LoggerFactory
import java.util.*

class ServiceImpl : Iservice {

    private val logger = LoggerFactory.getLogger(ServiceImpl::class.java)

    public fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, application: Application): V1Service? {
        val serviceName = user.login + "-" + application.name
        val newService = V1Service()
        newService.apiVersion = "v1"
        newService.kind = "Service"
        newService.metadata = createServiceMetadata(serviceName)
        newService.spec = createServiceSpec(serviceName, 80, "http-" + serviceName, "http-api")
        try {
            val service = api.createNamespacedService(namespace.metadata.name, newService, "true")
            logger.info("Service {} created", newService.metadata.name)
            return service
        } catch (exception: ApiException) {
            logger.error(exception.responseBody)
        }

        return newService
    }


//    nahradim qot tim o co se jedna -- napriklad preproces-modul a -m id uzivatele


    private fun createServiceSpec(selectorApp: String, servicePort: Int, servicePortName: String, targetPort: String): V1ServiceSpec {
        val spec = V1ServiceSpec()
        spec.selector = HashMap()
        spec.selector["app"] = selectorApp

        val port = V1ServicePort()
        port.port = servicePort
        port.name = servicePortName
        port.targetPort = IntOrString(targetPort)
        spec.ports = Arrays.asList(port)
        return spec
    }

    private fun createServiceMetadata(serviceName: String): V1ObjectMeta {
        val meta = V1ObjectMeta()
        meta.name = serviceName
        meta.annotations = createAnnotations("mapping-" + serviceName, "/" + serviceName + "/", serviceName)

        return meta
    }

    private fun createAnnotations(mappingName: String, urlPrefix: String, serviceToRun: String): HashMap<String, String> {
        val annotations = HashMap<String, String>()
        annotations.put("getambassador.io/config", "---\n" +
                "apiVersion: ambassador/v0\n" +
                "kind:  Mapping\n" +
                "name:  " + mappingName + "\n" +
                "prefix: " + urlPrefix + "\n" +
                "service: " + serviceToRun + "\n")

        return annotations
    }
}