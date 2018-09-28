package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.ModuleVersion
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.ModuleOperation
import cz.chobot.container_api.enum.ModuleType
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.exception.ImageBuilderException
import cz.chobot.container_api.kubernetes.KubernetesService
import cz.chobot.container_api.repository.ModuleRepository
import cz.chobot.container_api.repository.ModuleVersionRepository
import cz.chobot.container_api.service.IModuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.transaction.Transactional
import java.net.URI

@Service
open class ModuleService : IModuleService {


    @Autowired
    private lateinit var moduleRepository: ModuleRepository

    @Autowired
    private lateinit var moduleVersionRepository: ModuleVersionRepository

    @Autowired
    private lateinit var kubernetesService: KubernetesService

    @Value("\${image.buider.uri}")
    private val imageBuildUri: String? = null

    override fun createModule(module: Module, network: Network, user: User): Module {
        module.status = 0;
        return fillAndValidateModule(module, network, user, ModuleOperation.CREATE)
    }

    override fun updateModule(module: Module, network: Network, user: User): Module {
        module.status = 0;
        return fillAndValidateModule(module, network, user, ModuleOperation.UPDATE)
    }

    override fun deploy(module: Module, network: Network, user: User): Module {
        val deployedModule = kubernetesService.deployModule(module, user)
        val savedModule = moduleRepository.save(deployedModule)
        return savedModule
    }

    private fun createModuleVersion(module: Module): ModuleVersion {
        return moduleVersionRepository.save(module.actualVersion)
    }

    private fun setModuleType(module: Module): Int {
        return when (module.type) {
            ModuleType.LAMBDA.code -> ModuleType.LAMBDA.code
            ModuleType.REPOSITORY.code -> ModuleType.REPOSITORY.code
            ModuleType.IMAGE.code -> ModuleType.IMAGE.code
            else -> throw ControllerException("ER003")
        }
    }

    private fun checkModuleResponseClass(module: Module, network: Network) {
        network.modules.forEach { networkModule ->
            if (module.responseClass == networkModule.responseClass && module.id != networkModule.id) {
                throw ControllerException("ER008")
            }
        }

    }

    @Transactional
    open fun fillAndValidateModule(module: Module, network: Network, user: User, operation: ModuleOperation): Module {
        val regex = "._".toRegex()
        if (regex.containsMatchIn(module.name)) {
            throw ControllerException("ER009")
        }


        module.name = module.name.toLowerCase()
        checkModuleResponseClass(module, network)
        module.connectionPort = 5000
        module.type = setModuleType(module)
        module.network = network

        val savedModule = moduleRepository.save(module)
        savedModule.actualVersion.module = savedModule
        savedModule.actualVersion = createModuleVersion(savedModule)


        when (savedModule.type) {
            ModuleType.LAMBDA.code -> createDockerImage(savedModule, network, user, operation)
            ModuleType.REPOSITORY.code -> createDockerImage(savedModule, network, user, operation)
            ModuleType.IMAGE.code -> savedModule.imageId
            else -> throw ControllerException("ER003")
        }

        return savedModule
    }


    private fun createApiKey(): String{
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var apiKey = ""
        for (i in 0..31) {
            apiKey += chars[Math.floor(Math.random() * chars.length).toInt()]
        }

        return apiKey
    }

    private fun createDockerImage(module: Module, network: Network, user: User, operation: ModuleOperation) {
        val callResourceUrl = when (operation) {
            ModuleOperation.CREATE -> "${imageBuildUri}/network/${network.id}/user/${user.id}/module/${module.id}"
            ModuleOperation.UPDATE -> "${imageBuildUri}/network/${network.id}/user/${user.id}/module/${module.id}"
            ModuleOperation.RESTORE -> "${imageBuildUri}/network/${network.id}/user/${user.id}/module/${module.id}/restore"
            ModuleOperation.DELETE -> "${imageBuildUri}/network/${network.id}/user/${user.id}/module/${module.id}/restore"
        }

        val restTemplate = RestTemplate()
        val response = restTemplate.exchange(URI(callResourceUrl), operation.operation, null, Void::class.java)
        if (response.statusCode != HttpStatus.ACCEPTED) {
            throw ImageBuilderException("ER004")
        }
    }
}

