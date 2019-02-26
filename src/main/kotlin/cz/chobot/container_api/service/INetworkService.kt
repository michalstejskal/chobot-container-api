package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.User
import org.springframework.web.multipart.MultipartFile

interface INetworkService {
    fun createNetwork(network: Network, user: User): Network
    fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network
    fun setNetworkParameter(networkParam: NetworkParameter, network: Network, user: User): Network
    fun deploy(network: Network, user: User): Network
    fun getNetworkLogs(network: Network, user: User): String
    fun undeploy(network: Network, user: User): Network
    fun resetNetworkAttributes(network: Network): Network
}