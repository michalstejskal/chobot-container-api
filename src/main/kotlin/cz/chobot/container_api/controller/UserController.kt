package cz.chobot.container_api.controller

import cz.chobot.container_api.bo.User
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1/user")
class UserController {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: IUserService

    @GetMapping("/{idUser}")
    fun getUser(@PathVariable idUser: Long): ResponseEntity<User> {
        return userRepository.findById(idUser).map { user ->
            ResponseEntity.ok(user)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<Void> {
        val newUser = userService.createUser(user)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newUser.id)
                .toUri()
        return ResponseEntity.created(location).build()
    }

    @PutMapping
    fun updateUser(@RequestBody user: User): ResponseEntity<Void> {
        userService.updateUser(user)
        return ResponseEntity.ok().build()
    }

}