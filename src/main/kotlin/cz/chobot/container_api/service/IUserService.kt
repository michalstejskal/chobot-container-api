package cz.chobot.container_api.service

import cz.chobot.container_api.bo.User
import java.util.*

interface IUserService{
    fun findUser(idUser: Long): Optional<User>
    fun createUser(user: User): User
    fun updateUser(user: User): User
}