package cz.chobot.container_api.bo

import javax.persistence.Entity
import javax.persistence.Table
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "module_version")
data class ModuleVersion(
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "module_version_id", unique = true, nullable = false)
        val id: Long? = null,

        @NotNull
        @Size(max = 64)
        @Column(name = "name", nullable = false)
        var name: String,

        @Size(max = 256)
        @Column(name = "commit_id")
        var commitId: String? = null,

        @JsonIgnore
        @ManyToOne()
        @JoinColumn(name = "module_id")
        var module: Module

) {
    override fun hashCode(): Int {
        if (id == null) {
            return 0
        }
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ModuleVersion?
        return id == that?.id && name == that?.name
    }

    override fun toString(): String {
        return "$id - $name"
    }

}