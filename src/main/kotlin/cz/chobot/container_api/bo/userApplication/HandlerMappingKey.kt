package cz.chobot.container_api.bo.userApplication

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
data class HandlerMappingKey(

        @Column(name = "application_id")
        val applicationId: Long,

        @Column(name = "response_class")
        val responseClass: String,

        @NotNull
        @Column(name = "handler_name")
        val handlerName: String

): Serializable
