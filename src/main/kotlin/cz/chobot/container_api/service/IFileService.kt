package cz.chobot.container_api.service

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import org.springframework.web.multipart.MultipartFile

/***
 * Service for saving training data for networks
 */
interface IFileService{
    fun saveFileToPath(file: MultipartFile, network: Network, user: User): String
}