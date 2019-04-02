package cz.chobot.container_api.bo

import javax.persistence.Entity
import javax.persistence.Table
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "network_parameter")
data class NetworkParameter(
        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "network_parameter_id", unique = true, nullable = false)
        val id: Long? = null,

        @NotNull
        @Size(max = 64)
        @Column(name = "name", nullable = false)
        var name: String,

        @JsonIgnore
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "network_id")
        var network: Network? = null,

        @NotNull
        @Size(max = 64)
        @Column(name = "abbreviation", nullable = false)
        var abbreviation: String,

        @NotNull
        @Size(max = 1024)
        @Column(name = "value", nullable = false)
        var value: String
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
        val that = other as NetworkParameter?
        return id == that?.id && abbreviation == that?.abbreviation
    }

    override fun toString(): String {
        return "$id - $name"
    }
}