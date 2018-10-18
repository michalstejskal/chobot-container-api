package cz.chobot.container_api.kubernetes.deployment

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.models.ExtensionsV1beta1Deployment
import io.kubernetes.client.models.V1Namespace

interface IDeploymentService {
     fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, network: Network): String
     fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, module: Module): String
}