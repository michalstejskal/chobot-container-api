package cz.chobot.container_api.bo.userApplication

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "network_type")
data class NetworkType(
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "network_type_id", unique = true, nullable = false)
        val network_type_id: Int,

        @NotNull
        @Column(name = "name", nullable = false)
        val name: String
)