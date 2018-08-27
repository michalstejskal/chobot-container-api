package cz.chobot.container_api.kubernetes.namespace

import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1NamespaceList

interface INamespaceService {
    fun getAllNamespaces(api: CoreV1Api) :V1NamespaceList?
    fun getOrCreateNamespace(api: CoreV1Api, name: String): V1Namespace
}