package cz.chobot.container_api.kubernetes.namespace

import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1NamespaceList

interface INamespaceService {
    /***
     * gets all namespaces from kube cluster
     */
    fun getAllNamespaces(api: CoreV1Api) :V1NamespaceList?

    /***
     * Get namespace with specific name, if it's not exists -- create new one
     */
    fun getOrCreateNamespace(api: CoreV1Api, name: String): V1Namespace
}