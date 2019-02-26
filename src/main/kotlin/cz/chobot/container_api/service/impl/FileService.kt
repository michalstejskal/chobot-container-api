package cz.chobot.container_api.service.impl

import cz.chobot.container_api.bo.Network
import cz.chobot.container_api.bo.User
import cz.chobot.container_api.enum.NetworkTypeEnum
import cz.chobot.container_api.exception.ControllerException
import cz.chobot.container_api.service.IFileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.lang.System.`in`
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream




@Service
class FileService : IFileService {

    @Value("\${train.data.path}")
    private val filePath: String? = null

    override fun saveFileToPath(file: MultipartFile, network: Network, user: User): String {
        val path = Paths.get(filePath)
        val userPath = createUserPath(network, user)

        if (!file.isEmpty) {
            File("$filePath/$userPath").mkdirs()
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, path.resolve("$userPath/${file.originalFilename}"), StandardCopyOption.REPLACE_EXISTING)
            }
        } else {
            throw ControllerException("ER005 - FILE IS EMPTY")
        }

        return "$filePath/$userPath/${file.originalFilename}"
    }

    private fun isZipFile(filePath: String): Boolean {
        val file = File(filePath)
        val inFile = DataInputStream(BufferedInputStream(FileInputStream(file)))
        val test = inFile.readInt()
        inFile.close()
        return test == 0x504b0304;
    }

    private fun createUserPath(network: Network, user: User): String {
        return "${user.login}-${network.name}"
    }

}