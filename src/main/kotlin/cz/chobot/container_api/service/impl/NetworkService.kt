package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.NetworkStatus
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.kubernetes.IKubernetesService
import cz.chobot.container_api.repository.NetworkParameterRepository
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.IFileService
import cz.chobot.container_api.service.INetworkService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.crypto.SecretKey


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


    /***
     * Create and validate new network
     */
    override fun createNetwork(network: Network, user: User): Network {
        val validatedNetwork = validateAndSetUpNetwork(network, user)
        return networkRepository.save(validatedNetwork)
    }

    /***
     * Set training data to specified path and set this path to database
     */
    override fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network {
        val path = fileService.saveFileToPath(file, network, user)
        val existingParameter = networkParameterRepository.findByNetworkAndAbbreviation(network, "TRAIN_DATA_PATH")

        if (existingParameter.isPresent && existingParameter.get().size != 0) {
            val parameter = existingParameter.get()
            parameter.forEach { param ->
                if (param.abbreviation == "TRAIN_DATA_PATH") {
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

    /***
     * undeploy -- delete deployement and service from kube cluster
     */
    override fun undeploy(network: Network, user: User): Network {
        kubernetesService.undeployNetwork(network, user)
        val updatedNetwork = resetNetworkAttributes(network)
        logger.info("Network state updated ${user.login}-${updatedNetwork.name}")
        return updatedNetwork
    }


    /***
     * reset network parameter to default value eg. param if network is trained -- during undeploy is network Pod deleted so network is not trained
     */
    @Transactional
    override fun resetNetworkAttributes(network: Network): Network {
        network.status = NetworkStatus.CREATED.code
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


    /***
     * add new parameter like pygrok pattern
     */
    override fun setNetworkParameter(networkParam: NetworkParameter, network: Network, user: User): Network {
        val existingParameter = networkParameterRepository.findByNetworkAndAbbreviation(network, networkParam.abbreviation)

        if (existingParameter.isPresent && existingParameter.get().isNotEmpty()) {
            val parameter = existingParameter.get()
            parameter.forEach { param ->
                if (param.abbreviation == networkParam.abbreviation) {
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

    /***
     * Deploy network to kube cluster
     */
    override fun deploy(network: Network, user: User): Network {
        val deployedNetwork = kubernetesService.deployNetwork(network, user)
        return networkRepository.save(deployedNetwork)
    }

    /***
     * return network Pod logs
     */
    override fun getNetworkLogs(network: Network, user: User): String {
        val logs = kubernetesService.getPodLogs(network, user)
        val encodedData = Base64.getEncoder().encode(logs.toByteArray())
        val encdoded = String(encodedData, Charsets.UTF_8)
        return "{\"value\":\"$encdoded\"}"
    }


    /***
     * Check if network attrs are valid
     */
    private fun validateAndSetUpNetwork(network: Network, user: User): Network {
        network.name = createNetworkName(network)
        network.user = user

        // create secret and JWT token
        val key = createKeySecret()
        network.apiKeySecret = Base64.getEncoder().encodeToString(key.encoded)
        network.apiKey = createApiKey(user, network, key)

        // set Docker image id based on type
        val type = networkTypeRepository.findById(network.type.id)
        if (type.isPresent) {
            network.type = type.get()
            network.imageId = network.type.imageId
        } else {
            logger.error("unsupported type of network " + network.type.id)
            throw ControllerException("ER001 - unsupported network type")
        }


        // set connection uri for calls
        network.connectionUri = createConnectionUri(network, user)
        network.status = NetworkStatus.CREATED.code
        logger.info("network setted")
        return network
    }

    /***
     * Create and check network name
     */
    private fun createNetworkName(network: Network): String {
        // name should not have contain _ (name is used in url of network)
        val regex = "._".toRegex()
        if (regex.containsMatchIn(network.name)) {
            throw ControllerException("ER009")
        }

        return network.name.toLowerCase()
    }


    private fun createKeySecret(): SecretKey {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    private fun createApiKey(user: User, network: Network, key: SecretKey): String {

        return Jwts.builder()
                .setSubject("${user.login}-${network.name}")
                .claim("scope", "run")
                .claim("name", network.name)
                .setIssuedAt(Date())
                .signWith(key)
                .compact()
    }

    /***
     * create url for network
     */
    private fun createConnectionUri(network: Network, user: User): String {
        return "${user.login}-${network.name}"
    }


}