package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import org.springframework.web.multipart.MultipartFile

interface IFileService{
    fun saveFileToPath(file: MultipartFile, network: Network, user: User): String
}