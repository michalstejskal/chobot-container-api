package cz.chobot.container_api.service

import cz.chobot.container_api.bo.User
import java.util.*

interface IUserService{
    /***
     * find user by his id's
     */
    fun findUser(idUser: Long): Optional<User>

    /***
     * create new user
     */
    fun createUser(user: User): User

    /***
     * update user data
     */
    fun updateUser(user: User): User
}