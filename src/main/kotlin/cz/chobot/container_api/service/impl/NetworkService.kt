package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.NetworkStatus
import cz.chobot.container_api.enum.NetworkTypeEnum
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.kubernetes.IKubernetesService
import cz.chobot.container_api.kubernetes.KubernetesService
import cz.chobot.container_api.repository.NetworkParameterRepository
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.NetworkTypeRepository
import cz.chobot.container_api.service.IFileService
import cz.chobot.container_api.service.INetworkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*


@Service
class NetworkService : INetworkService {

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


    override fun createNetwork(network: Network, user: User): Network {
        val validatedNetwork = validateAndSetUpNetwork(network, user)
        return networkRepository.save(validatedNetwork)
    }

    override fun setTrainDataPath(file: MultipartFile, network: Network, user: User): Network {
        if (network.type.name != NetworkTypeEnum.CHATBOT.typeName) {
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
        } else {
            throw ControllerException("ER007")
        }
    }

    override fun setEncodedTrainData(encodedData: String, network: Network, user: User): Network {
        if (network.type.name == NetworkTypeEnum.CHATBOT.typeName) {
            val existingParameter = networkParameterRepository.findByNetworkAndAbbreviation(network, "DECODED_TRAIN_DATA")
            val decodedData = String(Base64.getDecoder().decode(encodedData))

            if (existingParameter.isPresent && existingParameter.get().size != 0) {
                val parameter = existingParameter.get()
                parameter.forEach { param ->
                    if (param.abbreviation.equals("DECODED_TRAIN_DATA")) {
                        param.value = decodedData
                        networkParameterRepository.save(param)
                    }
                }

                return networkRepository.findById(network.id).get()
            } else {
                network.parameters = mutableSetOf(NetworkParameter(name = "DECODED_TRAIN_DATA", abbreviation = "DECODED_TRAIN_DATA", value = decodedData, network = network))
                networkRepository.save(network)
                return network
            }
        } else {
            throw ControllerException("ER007")
        }
    }

    override fun deploy(network: Network, user: User): Network {
        return kubernetesService.deployNetwork(network, user)
    }


    private fun validateAndSetUpNetwork(network: Network, user: User): Network {
        network.name = network.name.toLowerCase()
        network.user = user
        val regex = "._".toRegex()
        if(regex.containsMatchIn(network.name)){
            throw ControllerException("ER009")
        }

        val type = networkTypeRepository.findById(network.type.id)

        if (type.isPresent) {
            network.type = type.get()
            network.imageId = network.type.imageId
        } else {
            throw ControllerException("ER001")
        }

        network.user = user
        network.connectionUri = createConnectionUri(network, user)
        network.status = NetworkStatus.CREATED.code
        return network
    }

    private fun createConnectionUri(network: Network, user: User): String {
        return "${user.login}/${network.name}"
    }


}