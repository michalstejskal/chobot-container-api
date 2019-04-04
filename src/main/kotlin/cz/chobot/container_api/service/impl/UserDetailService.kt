package cz.chobot.container_api.service.impl

import cz.chobot.container_api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailService : UserDetailsService {

    @Autowired
    private lateinit var userDao: UserRepository

    private val logger = LoggerFactory.getLogger(UserDetailService::class.java)

    override fun loadUserByUsername(login: String): UserDetails {
        logger.info("check user login $login")
        val userOpt = userDao.findByLogin(login)

        if (userOpt.isPresent) {
            logger.info("user login $login found")
            val user = userOpt.get()
            return User(user.login, user.password, emptyList())
        }
        throw UsernameNotFoundException(login);
    }

}