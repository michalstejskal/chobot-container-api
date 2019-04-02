package cz.chobot.container_api.kubernetes.service

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.models.V1Namespace
import io.kubernetes.client.models.V1Service

/***
 * Service to handle life cycle for Kube services for modules and networks
 */
interface Iservice {
    /***
     * Create new service for network with amabsador annotations
     * api -- api to create new service
     * namespace -- where new service create
     * user -- user object - used for service name
     * newtwork -- network obejct, contains all info about network
     */
    fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, network: Network): V1Service?

    /***
     * Create new service for module with amabsador annotations
     * api -- api to create new service
     * namespace -- where new service create
     * user -- user object - used for service name
     * module -- module obejct, contains all info about module
     */
    fun createService(api: CoreV1Api, namespace: V1Namespace, user: User, module: Module): V1Service?

    /***
     * Delete service for network/module by its selector
     */
    fun deleteService(api: CoreV1Api, namespace: V1Namespace, serviceName: String)
}