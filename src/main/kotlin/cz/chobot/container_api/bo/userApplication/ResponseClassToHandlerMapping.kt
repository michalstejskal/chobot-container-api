package cz.chobot.container_api.bo.userApplication

import com.fasterxml.jackson.annotation.JsonIgnore
import cz.chobot.container_api.bo.user.User
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "HandlerMapping")
data class ResponseClassToHandlerMapping(

        @EmbeddedId
        val id: HandlerMappingKey

//        @JsonIgnore
//        @ManyToOne(cascade = [CascadeType.ALL])
//        @JoinColumn(name = "application_id")
//        val application: Application,
//
//        @NotNull
//        @Column(name = "response_class", nullable = false)
//        val responseClass: String,
//
//        @NotNull
//        @Column(name = "handler_name", nullable = false)
//        val handlerName: String







)