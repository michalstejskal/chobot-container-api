package cz.chobot.container_api.controller

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.INetworkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user/{idUser}/network")
class NetworkController {

    @Autowired
    private lateinit var networkService: INetworkService

    @Autowired
    private lateinit var networkRepository: NetworkRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val logger = LoggerFactory.getLogger(NetworkController::class.java)


    @GetMapping
    fun getAllByUser(@PathVariable("idUser") idUser: Long): ResponseEntity<Set<Network>> {
        logger.info("get all networks for user $idUser")
        val user = userRepository.findById(idUser)
        if (user.isPresent) {
            val networks = networkRepository.findAllByUserIdOrderByNameAsc(idUser)
            return ResponseEntity(networks, null, HttpStatus.OK)
        }
        logger.info("no networks found for user $idUser")
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/{idNetwork}")
    fun getNetworkDetail(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<Network> {
        logger.info("get specific network $idNetwork for user $idUser")
        val user = userRepository.findById(idUser)
        if (user.isPresent) {
            val network = networkRepository.findByIdAndUserId(idNetwork, idUser)
            if (network.isPresent) {
                return ResponseEntity(network.get(), null, HttpStatus.OK)
            }
            logger.info("network not found$idNetwork")
            return ResponseEntity.notFound().build()
        }
        logger.info("user not found $idUser")
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/{idNetwork}/logs")
    fun getNetworkLogs(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<String> {
        logger.info("get logs of specific network $idNetwork for user $idUser")
        val user = userRepository.findById(idUser)
        if (user.isPresent) {
            val network = networkRepository.findByIdAndUserId(idNetwork, idUser)
            if (network.isPresent) {
                val logs = networkService.getNetworkLogs(network.get(), user.get())
                return ResponseEntity(logs, null, HttpStatus.OK)
            }
            logger.info("network not found$idNetwork")
            return ResponseEntity.notFound().build()
        }
        logger.info("user not found $idUser")
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/{idNetwork}/healtz")
    fun getNetworkHealth(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<String> {
        logger.info("get health of specific network $idNetwork for user $idUser")
        val user = userRepository.findById(idUser)
        if (user.isPresent) {
            val network = networkRepository.findByIdAndUserId(idNetwork, idUser)
            if (network.isPresent) {
                network.get().connectionUri
            }
            logger.info("network not found$idNetwork")
            return ResponseEntity.notFound().build()
        }
        logger.info("user not found $idUser")
        return ResponseEntity.notFound().build()
    }


    @PostMapping
    fun createNetwork(@PathVariable("idUser") idUser: Long, @Valid @RequestBody network: Network): ResponseEntity<Network> {
        logger.info("create new network for user $idUser")
        val user = userRepository.findById(idUser)

        if (user.isPresent) {
            val newNetwork = networkService.createNetwork(network, user.get())
            return ResponseEntity(newNetwork, HttpStatus.CREATED)
        }
        logger.info("User not found $idUser")
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/{idNetwork}/trainData")
    fun setTrainData(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<Void> {
        logger.info("set train data for network")
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            try {
                networkService.setTrainDataPath(file, network.get(), user.get())
                return ResponseEntity.ok().build()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/{idNetwork}/parameter")
    fun setNetworkParameter(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @RequestBody networkParam: NetworkParameter): ResponseEntity<Void> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            networkService.setNetworkParameter(networkParam, network.get(), user.get())
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }


    @PutMapping("/{idNetwork}")
    fun deploy(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<Network> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            val deployedNetwork = networkService.deploy(network.get(), user.get())
            return ResponseEntity(deployedNetwork, HttpStatus.OK)
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{idNetwork}")
    fun delete(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<Network> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            val updatedNetwork = networkService.undeploy(network.get(), user.get())
            return ResponseEntity(updatedNetwork, HttpStatus.OK)
        }
        return ResponseEntity.notFound().build()
    }

}