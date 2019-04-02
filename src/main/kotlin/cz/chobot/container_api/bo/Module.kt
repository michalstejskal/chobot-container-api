package cz.chobot.container_api.bo


import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "module")
data class
Module(
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "module_id", unique = true, nullable = false)
        val id: Long,

        @NotNull
        @Column(name = "type", nullable = false)
        var type: Int,

        @NotNull
        @Size(max = 64)
        @Column(name = "response_class", nullable = false)
        var responseClass: String,

        @NotNull
        @Column(name = "code", nullable = false)
        var code: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "name", nullable = false)
        var name: String,

        @OneToMany(cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER, mappedBy = "module")
        var versions: MutableSet<ModuleVersion>,

        @Size(max = 64)
        @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
        @JoinColumn(name = "module_version_id")
        var actualVersion: ModuleVersion,

        @NotNull
        @Column(name = "status", nullable = false)
        var status: Int,

        @Column(name = "repo_url")
        var repositoryUrl: String,

        @NotNull
        @Column(name = "connection_uri")
        var connectionUri: String,

        @NotNull
        @Column(name = "connection_uri_internal")
        var connectionUriInternal: String,

        @Column(name = "api_key")
        var apiKey: String,

        @NotNull
        @Column(name = "connection_port")
        var connectionPort: Int,

        @JsonIgnore
        @Column(name = "docker_registry")
        var dockerRegistry: String,

        @Column(name = "docker_image_id")
        var imageId: String,

        @Column(name = "docker_container_id")
        var containerId: String,

        @JsonIgnore
        @ManyToOne()
        @JoinColumn(name = "network_id")
        var network: Network,

        @Column(name = "api_key_secret")
        var apiKeySecret: String
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Module?
        return id == that?.id && name == that.name
    }

    override fun toString(): String {
        return "$id - $name"
    }
}