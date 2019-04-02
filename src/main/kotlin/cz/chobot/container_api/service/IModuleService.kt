package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import java.util.concurrent.CompletableFuture

interface IModuleService {
    /***
     * Create new module
     */
    fun createModule(module: Module, network: Network, user: User): Module

    /***
     * Update module
     */
    fun updateModule(module: Module, network: Network, user: User):Module

    /***
     * Deploy module to kube cluster
      */
    fun deploy(module: Module, network: Network, user: User): Module

    /***
     * Return logs of specific module Pod in kube cluster
     */
    fun getModuleLogs(module: Module, user: User): String

    /***
     * Delete deployments and service for module
     */
    fun undeployModule(module: Module, user: User): Module

}