package cz.chobot.container_api.controller

import cz.chobot.container_api.bo.NetworkType
import cz.chobot.container_api.repository.NetworkTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/networktype")
class NetworkTypeController {

    @Autowired
    private lateinit var networkTypeRepository: NetworkTypeRepository

    @GetMapping
    fun getAll(): ResponseEntity<Set<NetworkType>> {
        val networkTypes = networkTypeRepository.findAll()
        return ResponseEntity(networkTypes.toSet(), null, HttpStatus.OK)
    }
}

