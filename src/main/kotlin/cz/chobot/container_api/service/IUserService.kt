package cz.chobot.container_api.service

import cz.chobot.container_api.bo.User

interface IUserService{
    fun createUser(user: User): User
    fun updateUser(user: User): User
}