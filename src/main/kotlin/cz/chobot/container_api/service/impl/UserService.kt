package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService : IUserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    /***
     * find user by his id's
     */
    override fun findUser(idUser: Long): Optional<User> {
        val userOpt = userRepository.findById(idUser)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            return Optional.of(user)
        }
        return userOpt
    }

    /***
     * create new user
     */
    override fun createUser(user: User): User {
        user.login = user.login.toLowerCase()
        val bCryptPasswordEncoder = BCryptPasswordEncoder()
        user.password = bCryptPasswordEncoder.encode(user.password)
        // username is used in his network/module urls

        val regex = "._".toRegex()
        if (regex.containsMatchIn(user.login) || user.login.isEmpty()) {
            throw ControllerException("ER009 BAD USERNAME")
        }


        return userRepository.save(user)
    }

    /***
     * update user data
     */
    override fun updateUser(user: User): User {
        return userRepository.save(user)
    }

}