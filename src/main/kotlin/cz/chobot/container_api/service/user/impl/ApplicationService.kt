package cz.chobot.container_api.service.user.impl

import cz.chobot.container_api.bo.userApplication.Application
import cz.chobot.container_api.repository.ApplicationDao
import cz.chobot.container_api.service.user.IApplicationService
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class ApplicationService : IApplicationService {

    @Autowired
    private lateinit var applicationDao: ApplicationDao

    @Value("\${image.buider.uri}")
    private val builderUri: String? = null

    override fun createApplication(application: Application): Application {


        createNetwork()
        return application

    }

    private fun createNetwork(){

    }

}