package cz.chobot.container_api.controller

import cz.chobot.container_api.bo.Module
import cz.chobot.container_api.repository.ModuleRepository
import cz.chobot.container_api.repository.NetworkRepository
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.IModuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/user/{idUser}/network/{idNetwork}/module")
class ModuleController {

    @Autowired
    private lateinit var moduleService: IModuleService

    @Autowired
    private lateinit var networkRepository: NetworkRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var moduleRepository: ModuleRepository


    @GetMapping("/{idModule}")
    fun get(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @PathVariable("idModule") idModule: Long): ResponseEntity<Module> {
        val user = userRepository.findById(idUser)
        val module = moduleRepository.findById(idModule)
        val network = networkRepository.findById(idNetwork)

        if (module.isPresent && user.isPresent && network.isPresent) {
            return ResponseEntity<Module>(module.get(), null, HttpStatus.OK)
        }

        return ResponseEntity.notFound().build()
    }

    @PostMapping
    fun create(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @Valid @RequestBody module: Module): ResponseEntity<Void> {
        val user = userRepository.findById(idUser)
        val network = networkRepository.findById(idNetwork)

        if (network.isPresent && user.isPresent) {
            val newNetwork = moduleService.createModule(module, network.get(), user.get())
            val location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newNetwork.id)
                    .toUri()
            return ResponseEntity.created(location).build()
        }
        return ResponseEntity.notFound().build()
    }

    @PutMapping("/{idModule}")
    fun update(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @Valid @RequestBody module: Module): ResponseEntity<Void> {
        val user = userRepository.findById(idUser)
        val existingModule = moduleRepository.findById(module.id)
        val network = networkRepository.findById(idNetwork)

        if (existingModule.isPresent && user.isPresent && network.isPresent) {
            moduleService.updateModule(module, network.get(), user.get())
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.notFound().build()
    }



    @PutMapping("/{idModule}/deployment")
    fun deploy(@PathVariable("idUser") idUser: Long, @PathVariable("idNetwork") idNetwork: Long, @PathVariable("idModule") idModule: Long): ResponseEntity<String> {
        val user = userRepository.findById(idUser)
        val module = moduleRepository.findById(idModule)
        val network = networkRepository.findById(idNetwork)

        if (module.isPresent && user.isPresent && network.isPresent) {
            val deployedModule = moduleService.deploy(module.get(), network.get(), user.get())
            return ResponseEntity.ok(deployedModule.connectionUri)
        }
        return ResponseEntity.notFound().build()
    }
}