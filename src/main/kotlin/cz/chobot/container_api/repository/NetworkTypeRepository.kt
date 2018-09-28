package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.NetworkType
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

@Transactional
interface NetworkTypeRepository : JpaRepository<NetworkType, Long>
