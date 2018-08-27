package cz.chobot.container_api.service.user

import cz.chobot.container_api.bo.userApplication.Application

interface IApplicationService{
    fun createApplication(application: Application): Application
}