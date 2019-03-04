package cz.chobot.container_api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDto(@JsonProperty("username") val username: String, @JsonProperty("password") val password: String){}