package cz.chobot.container_api.bo

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import java.awt.print.Book




@Entity
@Table(name = "chobot_user")
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "user_id")
        val id: Long,

        @NotNull
        @Size(max = 64)
        @Column(unique = true, name = "login")
        var login: String,

        @JsonIgnore
        @NotNull
        @Size(max = 32)
        @Column(name = "password")
        var password: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "first_name")
        var firstName: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "last_name")
        var lastName: String,

        @NotNull
        @Size(max = 64)
        @Column(name = "email")
        val email: String,

//        @NotNull
//        @Size(max = 256)
//        @Column(name = "secret")
//        val secret: String,

        @JsonIgnore
        @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = [CascadeType.ALL])
        val networks: MutableSet<Network>
){
        override fun hashCode(): Int {
                return id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || javaClass != other.javaClass) return false
                val that = other as User?
                return id == that?.id && login == that?.login
        }

        override fun toString(): String {
                return "$id - $login"
        }
}


