package cz.chobot.container_api.kubernetes.namespace.impl

import cz.chobot.container_api.kubernetes.namespace.INamespaceService
import io.kubernetes.client.ApiException
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1ObjectMeta
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import io.kubernetes.client.models.V1NamespaceList
import org.bouncycastle.asn1.x500.style.RFC4519Style.name


@Service
class NamespaceService : INamespaceService {
    private val logger = LoggerFactory.getLogger(NamespaceService::class.java)

    override fun getAllNamespaces(api: CoreV1Api): V1NamespaceList? {
        return api.listNamespace(true, null, null, null, null, null, null, null, null)

    }

    override fun getOrCreateNamespace(api: CoreV1Api, name: String): V1Namespace {

        val existingNamespace = getExistingNamespace(api, name)
        if (existingNamespace != null) {
            logger.info("Namespace {} found in kubernetes cluster", name)
            return existingNamespace
        }

        try {
            logger.info("Creating new namespace {}.", name)
            val metadata = V1ObjectMeta()
            metadata.name = name

            val namespaceBody = V1Namespace()
            namespaceBody.metadata = metadata

            val namespace: V1Namespace = api.createNamespace(namespaceBody, true, "true", "true")
            logger.info("Namespace {} created.", name)
            return namespace
        } catch (exception: ApiException) {
            logger.info("Error occurred while creating namespace {}. Error is: {}", name, exception.responseBody)
        }

        return V1Namespace()
    }


    private fun getExistingNamespace(api: CoreV1Api, name: String): V1Namespace? {
        try {
            return api.readNamespace(name, "true", true, false)
        } catch (exception: ApiException) {
            logger.error("Namespce {} not found", name)
        }

        return null
    }

}

