package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import org.springframework.web.multipart.MultipartFile

interface INetworkService {
    fun createNetwork(network: Network, user: User): Network
    fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network
    fun setEncodedTrainData(encodedData: String, network: Network, user: User): Network
    fun deploy(network: Network, user: User): Network
    fun getNetworkLogs(network: Network, user: User): String
}