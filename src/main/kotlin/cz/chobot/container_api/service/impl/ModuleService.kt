package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.bo.ModuleVersion
import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.ModuleOperation
import cz.chobot.container_api.enum.ModuleStatus
import cz.chobot.container_api.enum.ModuleType
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.exception.ImageBuilderException
import cz.chobot.container_api.kubernetes.KubernetesService
import cz.chobot.container_api.repository.ModuleRepository
import cz.chobot.container_api.repository.ModuleVersionRepository
import cz.chobot.container_api.service.IModuleService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.apache.logging.log4j.util.Strings
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.transaction.Transactional
import java.net.URI
import java.util.*
import javax.crypto.SecretKey

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

    private val logger = LoggerFactory.getLogger(ModuleService::class.java)

    override fun createModule(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.CREATED.code;
        return fillAndValidateModule(module, network, user, ModuleOperation.CREATE)
    }

    override fun updateModule(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.CREATED.code;
        return fillAndValidateModule(module, network, user, ModuleOperation.UPDATE)
    }

    override fun deploy(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.DEPLOYED.code
        val deployedModule = kubernetesService.deployModule(module, user)
        val savedModule = moduleRepository.save(deployedModule)
        return savedModule
    }

    override fun getModuleLogs(module: Module, user: User): String{
        val logs =  kubernetesService.getPodLogs(module, user)
        val encodedData = Base64.getEncoder().encode(logs.toByteArray())
        val encdoded = String(encodedData, Charsets.UTF_8)
        return "{\"value\":\"$encdoded\"}"
    }

    override fun undeployModule(module: Module, user: User): Module {
        kubernetesService.undeployModule(module, user)
        module.status = ModuleStatus.CREATED.code
        module.connectionUri = Strings.EMPTY
        moduleRepository.save(module)
        logger.info("Network state updated ${user.login}-${module.name}")
        return module
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

        val key = createKeySecret()
        module.apiKeySecret = Base64.getEncoder().encodeToString(key.getEncoded())
        module.apiKey = createApiKey(user, module, key)


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

    private fun createKeySecret(): SecretKey {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    private fun createApiKey(user: User, module: Module, key: SecretKey): String {

        return Jwts.builder()
                .setSubject("${user.login}-${module.name}")
                .claim("scope", "run")
                .claim("name", module.name)
                .setIssuedAt(Date())
                .signWith(key)
                .compact()
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

