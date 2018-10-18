package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.User
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.repository.UserRepository
import cz.chobot.container_api.service.IUserService
import org.apache.catalina.mbeans.UserMBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService : IUserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    override fun findUser(idUser: Long): Optional<User> {
        val userOpt = userRepository.findById(idUser)
        if(userOpt.isPresent){
            val user = userOpt.get()
            return Optional.of(user)
        }
        return userOpt
    }

    override fun createUser(user: User): User {
        user.login = user.login.toLowerCase()

        val regex = "._".toRegex()
        if(regex.containsMatchIn(user.login)){
            throw ControllerException("ER009")
        }

        return userRepository.save(user)
    }

    override fun updateUser(user: User): User {
        return userRepository.save(user)
    }

}