package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import java.util.concurrent.CompletableFuture

interface IModuleService {
    fun createModule(module: Module, network: Network, user: User): Module
    fun updateModule(module: Module, network: Network, user: User):Module
    fun deploy(module: Module, network: Network, user: User): Module
    fun getModuleLogs(module: Module, user: User): String
    fun undeployModule(module: Module, user: User): Module

}