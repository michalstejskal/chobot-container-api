package cz.chobot.container_api.service.user

import cz.chobot.container_api.bo.user.User

interface IUserService{
    fun createUser(user: User): User
    fun updateUser(user: User): User
}