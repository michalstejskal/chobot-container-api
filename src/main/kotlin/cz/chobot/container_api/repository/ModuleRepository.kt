package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.Module
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

@Transactional
interface ModuleRepository: JpaRepository<Module, Long>{
    fun findAllByNetworkId(idNetwork: Long): Set<Module>
}
