package cz.chobot.container_api.controller

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.INetworkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
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

    @PostMapping
    fun create(@PathVariable("idUser") idUser: Long, @Valid @RequestBody network: Network): ResponseEntity<Void> {

        val user = userRepository.findById(idUser)

        if (user.isPresent) {
            val newNetwork = networkService.createNetwork(network, user.get())
            val location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newNetwork.id)
                    .toUri()
            return ResponseEntity.created(location).build()
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/{idNetwork}/trainData")
    fun setTrainData(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<Void> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            networkService.setTrainDataPath(file, network.get(), user.get())
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/{idNetwork}/encodedTrainData")
    fun setEncodedTrainData(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @RequestBody encodedData: String): ResponseEntity<Void> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            networkService.setEncodedTrainData(encodedData, network.get(), user.get())
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }


    @PutMapping("/{idNetwork}")
    fun deploy(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<Void> {
        val network = networkRepository.findById(idNetwork)
        val user = userRepository.findById(idUser)
        if (network.isPresent && user.isPresent) {
            val deployedNetwork = networkService.deploy(network.get(), user.get())
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping
    fun delete(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long): ResponseEntity<Void> {
        TODO("dodelat")
    }

}