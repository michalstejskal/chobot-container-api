package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.User
import org.springframework.web.multipart.MultipartFile

interface INetworkService {
    /***
     * Create new Network from user data
     */
    fun createNetwork(network: Network, user: User): Network

    /***
     * Set train data for service
     */
    fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network

    /***
     * add new parameter like pygrok pattern
     */
    fun setNetworkParameter(networkParam: NetworkParameter, network: Network, user: User): Network

    /***
     * create new deployment and service
     */
    fun deploy(network: Network, user: User): Network

    /***
     * get network Pod logs by ites selector
     */
    fun getNetworkLogs(network: Network, user: User): String

    /***
     * remove network deployment and service
     */
    fun undeploy(network: Network, user: User): Network

    /***
     * reset network parameter to default value eg. param if network is trained -- during undeploy is network Pod deleted so network is not trained
     */
    fun resetNetworkAttributes(network: Network): Network
}