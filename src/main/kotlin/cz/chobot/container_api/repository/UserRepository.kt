package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

@Transactional
internal interface UserRepository : JpaRepository<User, Long>{
    fun findByLogin(login: String): Optional<User>
}