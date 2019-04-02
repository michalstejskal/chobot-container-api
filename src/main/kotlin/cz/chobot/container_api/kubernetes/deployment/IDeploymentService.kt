package cz.chobot.container_api.kubernetes.deployment

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import io.kubernetes.client.apis.ExtensionsV1beta1Api
import io.kubernetes.client.models.V1Namespace
/***
 * Service to handle life cycle for Kube deployments for modules and networks
 */
interface IDeploymentService {
    /***
     * Create new deploymnet for network
     * api -- api which is called to crate deployment
     * namespace -- kube namespace where create new deployment
     * user -- user object -- for deployment name
     * network -- network with networkType
     * return deploymentName if successful, empty string if error
     */
    fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, network: Network): String

    /***
     * Create new deploymnet for module
     * api -- api which is called to crate deployment
     * namespace -- kube namespace where create new deployment
     * user -- user object -- for deployment name
     * module -- module with docker image id
     * return deploymentName if successful, empty string if error
     */
    fun createDeploymentForService(api: ExtensionsV1beta1Api, namespace: V1Namespace, user: User, module: Module): String

    /***
     * Delete deployment by its selector
     * api -- api which is called to delete deployment
     * namespace -- kube namespace where delete deployment
     * name -- deployment selector
     */
    fun deleteDeployment(api: ExtensionsV1beta1Api, namespace: V1Namespace, deploymentName: String)
}