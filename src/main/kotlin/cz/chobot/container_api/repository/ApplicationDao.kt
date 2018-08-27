package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.userApplication.Application
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

@Transactional
internal interface ApplicationDao : JpaRepository<Application, Long> {


    fun findByUser_Id(userId: Long): List<Application>
    fun findByIdAndUser_Id(id: Long, userId: Long): Optional<Application>
}