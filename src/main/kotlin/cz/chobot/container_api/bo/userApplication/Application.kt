package cz.chobot.container_api.bo.userApplication

import cz.chobot.container_api.bo.user.User
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import com.fasterxml.jackson.annotation.JsonIgnore


@Entity
@Table(name = "application")
data class Application(

        @NotNull
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "application_id", unique = true, nullable = false)
        val id: Long,

        @NotNull
        @Size(max = 64)
        @Column(name = "name", nullable = false)
        val name: String,

        @Size(max = 64)
        @Column(name = "description")
        val description: String? = null,

        @NotNull
        @Column(name = "type")
        val type: Int,

        @NotNull
        @Column(name = "state")
        val state: Int,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "user_id")
        val user: User,

        @JsonIgnore
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "module_id")
        val module: Module,

        @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
        @JoinColumn(name = "network_id")
        val network: Network

)