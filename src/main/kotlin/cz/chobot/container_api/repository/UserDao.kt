package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.user.User
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

@Transactional
internal interface UserDao : JpaRepository<User, Long>