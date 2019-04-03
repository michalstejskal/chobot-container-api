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

    /***
     * Create new module and validate its data
     */
    override fun createModule(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.CREATED.code
        return fillAndValidateModule(module, network, user, ModuleOperation.CREATE)
    }

    /***
     *  moupdatedule and validate its data
     */
    override fun updateModule(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.CREATED.code
        return fillAndValidateModule(module, network, user, ModuleOperation.UPDATE)
    }


    /***
     * Deploy module to kube cluster -- new Deployment and service
     */
    override fun deploy(module: Module, network: Network, user: User): Module {
        module.status = ModuleStatus.DEPLOYED.code
        val deployedModule = kubernetesService.deployModule(module, user)
        // save new module status and url
        return moduleRepository.save(deployedModule)
    }

    /***
     * get module Pod logs
     */
    override fun getModuleLogs(module: Module, user: User): String{
        val logs =  kubernetesService.getPodLogs(module, user)
        val encodedData = Base64.getEncoder().encode(logs.toByteArray())
        val encdoded = String(encodedData, Charsets.UTF_8)
        return "{\"value\":\"$encdoded\"}"
    }


    /***
     * Delete deployment and service by selector in kube cluster
     */
    override fun undeployModule(module: Module, user: User): Module {
        kubernetesService.undeployModule(module, user)
        module.status = ModuleStatus.CREATED.code
        module.connectionUri = Strings.EMPTY
        moduleRepository.save(module)
        logger.info("Network state updated ${user.login}-${module.name}")
        return module
    }


    /***
     * Create new module version
     */
    private fun createModuleVersion(module: Module): ModuleVersion {
        return moduleVersionRepository.save(module.actualVersion)
    }

    /***
     * set module type
     */
    private fun setModuleType(module: Module): Int {
        return when (module.type) {
            ModuleType.LAMBDA.code -> ModuleType.LAMBDA.code
            ModuleType.REPOSITORY.code -> ModuleType.REPOSITORY.code
            ModuleType.IMAGE.code -> ModuleType.IMAGE.code
            else -> throw ControllerException("ER003")
        }
    }

    /***
     * Check if there is no module fot it's user with same response class -> network call only one module
     */
    private fun checkModuleResponseClass(module: Module, network: Network) {
        network.modules.forEach { networkModule ->
            if (module.responseClass == networkModule.responseClass && module.id != networkModule.id) {
                throw ControllerException("ER008")
            }
        }

    }

    /***
     * Check module data if valid
     */
    @Transactional
    open fun fillAndValidateModule(module: Module, network: Network, user: User, operation: ModuleOperation): Module {
        // name should not have contain _ (name is used in url of module)
        val regex = "._".toRegex()
        if (regex.containsMatchIn(module.name)) {
            throw ControllerException("ER009 - BAD MODULE NAME")
        }

        // convert to lower case
        module.name = module.name.toLowerCase()
        checkModuleResponseClass(module, network)

        // set port for module flask api
        module.connectionPort = 5000
        module.type = setModuleType(module)
        module.network = network


        // crate secret and JWT token
        val key = createKeySecret()
        module.apiKeySecret = Base64.getEncoder().encodeToString(key.encoded)
        module.apiKey = createApiKey(user, module, key)


        // save module as actual version
        val savedModule = moduleRepository.save(module)
        savedModule.actualVersion.module = savedModule
        savedModule.actualVersion = createModuleVersion(savedModule)

        // when saved -> call image_builder to crate new module Docker image
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

    /***
     * call image_builder to crate new module Docker image
     */
    private fun createDockerImage(module: Module, network: Network, user: User, operation: ModuleOperation) {
        val callResourceUrl = when (operation) {
            ModuleOperation.CREATE -> "$imageBuildUri/network/${network.id}/user/${user.id}/module/${module.id}"
            ModuleOperation.UPDATE -> "$imageBuildUri/network/${network.id}/user/${user.id}/module/${module.id}"
            ModuleOperation.RESTORE -> "$imageBuildUri/network/${network.id}/user/${user.id}/module/${module.id}/restore"
            ModuleOperation.DELETE -> "$imageBuildUri/network/${network.id}/user/${user.id}/module/${module.id}/restore"
        }

        val restTemplate = RestTemplate()
        val response = restTemplate.exchange(URI(callResourceUrl), operation.operation, null, Void::class.java)
        if (response.statusCode != HttpStatus.ACCEPTED) {
            throw ImageBuilderException("ER004")
        }
    }
}

