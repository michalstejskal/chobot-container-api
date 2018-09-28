package cz.chobot.container_api.bo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "network")
data class Network (
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "network_id", unique = true, nullable = false)
        val id: Long,

        @NotNull
        @OneToOne
        @JoinColumn(name = "network_type_id")
        var type: NetworkType,

        @NotNull
        @Size(max = 64, min = 2, message = "Name should have at least 2 characters and max length is 64.")
        @Column(name = "name", nullable = false)
        var name: String,

        @JsonIgnore
        @Size(max = 64)
        @Column(name = "commit_id")
        var commitId: String,

        @JsonIgnore
        @Column(name = "docker_image_id")
        var imageId: String,

        @JsonIgnore
        @Column(name = "docker_container_id")
        var containerId: String,

        // 1 created
        // 2 set train data
        // 3 training finished
        // 4 deployed
        @NotNull
        @Column(name = "status", nullable = false)
        var status: Int,

        @NotNull
        @Column(name = "api_key", nullable = false)
        var apiKey: String,

        @JsonIgnore
        @Column(name = "connection_uri")
        var connectionUri: String,

        @JsonIgnore
        @Column(name = "docker_registry")
        var dockerRegistry: String,

        @OneToMany(fetch = FetchType.EAGER, mappedBy = "network", cascade = [CascadeType.REMOVE])
        var modules: MutableSet<Module>,

        @JsonIgnore
        @OneToMany(fetch = FetchType.EAGER, mappedBy = "network", cascade = [CascadeType.ALL])
        var parameters: MutableSet<NetworkParameter>,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        var user: User
) {
    override fun hashCode(): Int {
            return id.hashCode()
    }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || javaClass != other.javaClass) return false
                val that = other as Network?
                return id == that?.id && name == that?.name
        }

        override fun toString(): String {
                return "$id - $name"
        }
}


