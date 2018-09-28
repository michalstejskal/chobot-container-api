package cz.chobot.container_api.repository


import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.NetworkParameter
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

@Transactional
interface NetworkParameterRepository : JpaRepository<NetworkParameter, Long> {
    fun findByNetworkAndAbbreviation(network: Network, abbreviation: String): Optional<List<NetworkParameter>>
}
