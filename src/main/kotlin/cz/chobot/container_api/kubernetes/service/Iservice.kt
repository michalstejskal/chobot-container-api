package cz.chobot.container_api.kubernetes.service

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1Service

interface Iservice {
    fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, network: Network): V1Service?
    fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, module: Module): V1Service?
    fun deleteService(api: CoreV1Api, namespace: V1Namespace, serviceName: String)
}