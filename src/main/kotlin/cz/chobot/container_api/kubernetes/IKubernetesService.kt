package cz.chobot.container_api.kubernetes

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User

interface IKubernetesService{
    fun deployNetwork(network: Network, user: User): Network
    fun deployModule(module: Module, user: User): Module
    fun getPodLogs(network: Network, user: User): String
    fun getPodLogs(module: Module, user: User): String
}