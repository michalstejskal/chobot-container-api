package cz.chobot.container_api.bo.userApplication

import com.fasterxml.jackson.annotation.JsonIgnore
import java.rmi.registry.LocateRegistry
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "module")
data class Module(
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "module_id", unique = true, nullable = false)
        val id: Long,

        @NotNull
        @Column(name = "type", nullable = false)
        val type: Int,

        @NotNull
        @Size(max = 64)
        @Column(name = "response_class", nullable = false)
        val responseClass: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "name", nullable = false)
        val name: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "tag", nullable = false)
        val tag: String,

        @NotNull
        @Column(name = "status", nullable = false)
        val status: Int,

        @NotNull
        @Column(name = "connection_uri", nullable = false)
        val connectionUri: String,

        @NotNull
        @Column(name = "docker_registry", nullable = false)
        val dockerRegistry: String,

        @JsonIgnore
        @OneToOne(fetch=FetchType.LAZY, mappedBy="module")
        val application: Application
)