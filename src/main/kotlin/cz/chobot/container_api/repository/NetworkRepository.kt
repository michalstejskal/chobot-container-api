package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.Network
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

@Transactional
interface NetworkRepository: JpaRepository<Network, Long>{
    fun findAllByUserId(idUser: Long): Set<Network>
    fun findByIdAndUserId(id: Long,idUser: Long): Optional<Network>
}