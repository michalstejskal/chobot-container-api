package cz.chobot.container_api.bo.user

import cz.chobot.container_api.bo.userApplication.Application
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
@Table(name = "user")
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "user_id")
        val id: Long? = null,

        @NotNull
        @Size(max = 64)
        @Column(unique = true, name = "login")
        val login: String,

        @NotNull
        @Size(max = 32)
        @Column(name = "password")
        val password: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "first_name")
        val firstName: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "last_name")
        val lastName: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "email")
        val email: String,

        @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = [CascadeType.ALL])
        val applications: List<Application>
)
