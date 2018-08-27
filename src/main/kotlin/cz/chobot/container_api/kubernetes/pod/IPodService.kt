package cz.chobot.container_api.kubernetes.pod

import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1Pod
import io.kubernetes.client.models.V1Service

interface IPodService{
    fun createPod(api: CoreV1Api, namespace: V1Namespace): V1Pod?

    fun createService(api: CoreV1Api, namespace: V1Namespace): V1Service?

    fun findPod(api: CoreV1Api, namespace: V1Namespace, podName: String): V1Pod?

    fun deletePod(api: CoreV1Api, namespace: V1Namespace, pod: V1Pod)


    fun listPods(api: CoreV1Api)
    fun listServices(api: CoreV1Api)
}