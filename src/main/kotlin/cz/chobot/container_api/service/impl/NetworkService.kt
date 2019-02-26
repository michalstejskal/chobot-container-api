package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.NetworkStatus
import cz.chobot.container_api.enum.NetworkTypeEnum
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.kubernetes.IKubernetesService
import cz.chobot.container_api.repository.NetworkParameterRepository
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.IFileService
import cz.chobot.container_api.service.INetworkService
import org.apache.logging.log4j.util.Strings
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*



@Service
open class NetworkService : INetworkService {

    @Autowired
    private lateinit var networkRepository: NetworkRepository

    @Autowired
    private lateinit var networkTypeRepository: NetworkTypeRepository

    @Autowired
    private lateinit var fileService: IFileService

    @Autowired
    private lateinit var networkParameterRepository: NetworkParameterRepository

    @Autowired
    private lateinit var kubernetesService: IKubernetesService

    private val logger = LoggerFactory.getLogger(NetworkService::class.java)


    override fun createNetwork(network: Network, user: User): Network {
        val validatedNetwork = validateAndSetUpNetwork(network, user)
        return networkRepository.save(validatedNetwork)
    }

    override fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network {
        val path = fileService.saveFileToPath(file, network, user)
        val existingParameter = networkParameterRepository.findByNetworkAndAbbreviation(network, "TRAIN_DATA_PATH")

        if (existingParameter.isPresent && existingParameter.get().size != 0) {
            val parameter = existingParameter.get()
            parameter.forEach { param ->
                if (param.abbreviation.equals("TRAIN_DATA_PATH")) {
                    param.value = path
                    networkParameterRepository.save(param)
                }
            }

            return networkRepository.findById(network.id).get()
        } else {
            network.parameters = mutableSetOf(NetworkParameter(name = "TRAIN_DATA_PATH", abbreviation = "TRAIN_DATA_PATH", value = path, network = network))
            networkRepository.save(network)
            return network
        }
    }

    override fun undeploy(network: Network, user: User): Network {
        kubernetesService.undeployNetwork(network, user)
        val updatedNetwork = resetNetworkAttributes(network)
        logger.info("Network state updated ${user.login}-${updatedNetwork.name}")
        return updatedNetwork
    }

    @Transactional
    override fun resetNetworkAttributes(network: Network): Network {
        network.status = NetworkStatus.CREATED.code
        network.apiKeySecret = Strings.EMPTY
        network.connectionUri = Strings.EMPTY
        val params = network.parameters.filter { param -> param.abbreviation == "IS_TRAINED" }
        if (params.isNotEmpty()) {
            val isTrainedParam = params.first()
            network.parameters.remove(isTrainedParam)
            networkRepository.save(network)
            networkParameterRepository.delete(isTrainedParam)
        } else {
            networkRepository.save(network)
        }

        return network
    }

    override fun setNetworkParameter(networkParam: NetworkParameter, network: Network, user: User): Network {
        val existingParameter = networkParameterRepository.findByNetworkAndAbbreviation(network, networkParam.abbreviation)

        if (existingParameter.isPresent && existingParameter.get().size != 0) {
            val parameter = existingParameter.get()
            parameter.forEach { param ->
                if (param.abbreviation.equals(networkParam.abbreviation)) {
                    param.value = networkParam.value
                    networkParameterRepository.save(param)
                }
            }

            return networkRepository.findById(network.id).get()
        } else {
            networkParam.network = network
            network.parameters = mutableSetOf(networkParam)
            networkRepository.save(network)
            return network
        }

    }

    override fun deploy(network: Network, user: User): Network {
        val deployedNetwork = kubernetesService.deployNetwork(network, user)
        deployedNetwork.apiKeySecret = createApiKeySecret()
        deployedNetwork.apiKey = createApiKey(network)
        val savedNetwork = networkRepository.save(deployedNetwork)
        return savedNetwork
    }

    override fun getNetworkLogs(network: Network, user: User): String {
        val logs = kubernetesService.getPodLogs(network, user)
        val encodedData = Base64.getEncoder().encode(logs.toByteArray())
        val encdoded = String(encodedData, Charsets.UTF_8)
        return "{\"value\":\"$encdoded\"}"
    }

    private fun validateAndSetUpNetwork(network: Network, user: User): Network {
        network.name = createNetworkName(network)
        network.user = user

        val type = networkTypeRepository.findById(network.type.id)
        if (type.isPresent) {
            network.type = type.get()
            network.imageId = network.type.imageId
        } else {
            logger.error("unsupported type of network " + network.type.id)
            throw ControllerException("ER001 - unsupported network type")
        }

        network.connectionUri = createConnectionUri(network, user)
        network.status = NetworkStatus.CREATED.code
        logger.info("network setted")
        return network
    }

    private fun createNetworkName(network: Network): String {
        val regex = "._".toRegex()
        if (regex.containsMatchIn(network.name)) {
            throw ControllerException("ER009")
        }

        return network.name.toLowerCase()
    }

    private fun createApiKeySecret(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var apiKeyPwd = ""
        for (i in 0..31) {
            apiKeyPwd += chars[Math.floor(Math.random() * chars.length).toInt()]
        }

        val encodedApiKey = Base64.getEncoder().encode(apiKeyPwd.toByteArray())
        return String(encodedApiKey, Charsets.UTF_8)
    }

    private fun createApiKey(network: Network): String {
        val rootObject = JSONObject()
        rootObject.put("network_id", network.id)
        rootObject.put("api_key", network.apiKeySecret)

        val encodedApiKey = Base64.getEncoder().encode(rootObject.toString().toByteArray())
        return String(encodedApiKey, Charsets.UTF_8)
    }

    private fun createConnectionUri(network: Network, user: User): String {
        return "${user.login}-${network.name}"
    }


}