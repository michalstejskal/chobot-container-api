package cz.chobot.container_api.repository

import cz.chobot.container_api.bo.ModuleVersion
import org.springframework.data.jpa.repository.JpaRepository
import javax.transaction.Transactional

@Transactional
interface ModuleVersionRepository: JpaRepository<ModuleVersion, Long>