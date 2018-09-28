package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.Network
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

@Transactional
interface NetworkRepository: JpaRepository<Network, Long>