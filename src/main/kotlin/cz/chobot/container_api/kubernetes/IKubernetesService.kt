package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User


/***
 * Wrapper for all interactions with Kube cluster
 */
interface IKubernetesService{

    /***
     * Create new deployments and service for Network
     */
    fun deployNetwork(network: Network, user: User): Network
    /***
     * Create new deployments and service for Module
     */
    fun deployModule(module: Module, user: User): Module

    /***
     * Connects to Kube cluster and get logs of specific POD with selector -- network
     */
    fun getPodLogs(network: Network, user: User): String

    /***
     * Connects to Kube cluster and get logs of specific POD with selector -- module
     */
    fun getPodLogs(module: Module, user: User): String

    /***
     * Delete deployment and service by thirs selector -- network
     */
    fun undeployNetwork(network: Network, user: User)

    /***
     * Delete deployment and service by thirs selector -- module
     */
    fun undeployModule(module: Module, user: User)
}