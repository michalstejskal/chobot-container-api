package cz.chobot.container_api.service.user.impl

import cz.chobot.container_api.bo.user.User
import cz.chobot.container_api.repository.UserDao
import cz.chobot.container_api.service.user.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService : IUserService {


    @Autowired
    private lateinit var userDao: UserDao

    override fun createUser(user: User): User {
        return userDao.save(user)
    }

    override fun updateUser(user: User): User {
        return userDao.save(user)
    }

}