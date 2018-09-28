package cz.chobot.container_api

//import cz.chobot.container_api.kubernetes.deployment.impl.DeploymentServiceImpl
//import cz.chobot.container_api.kubernetes.namespace.impl.NamespaceService
//import cz.chobot.container_api.kubernetes.pod.impl.PodService
//import cz.chobot.container_api.kubernetes.service.impl.ServiceImpl

import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.util.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.kubernetes.client.Configuration
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
open class ContainerApiApplication


private val logger = LoggerFactory.getLogger(ContainerApiApplication::class.java)


//private lateinit var namespaceService: NamespaceService
//private lateinit var podService: PodService
//private lateinit var deploymentService: DeploymentServiceImpl
//private lateinit var service: ServiceImpl


fun getApi(): CoreV1Api {
    // setting configuration
    val client = Config.defaultClient()
    Configuration.setDefaultApiClient(client)
    val api = CoreV1Api()
    return api
}


//fun fakespring() {
//    namespaceService = NamespaceService()
//    podService = PodService()
//    deploymentService = DeploymentServiceImpl()
//    service = ServiceImpl()
//
//}

fun main(args: Array<String>) {
    runApplication<ContainerApiApplication>(*args)
//    fakespring()

//    val api = getApi()
//    val namespace = namespaceService.getOrCreateNamespace(api, "chobot-namespace")
//    val namespace = namespaceService.getOrCreateNamespace(api, "default")


//    api.readNamespacedPodLog()
//    val user = findUser("stejskys")
//    val app = createApplication("image-classification-newwww")


//    logger.info("creating application {} ", user.login + "-" + app.name)
//    val service = service.createService(api, namespace, user, app)
//    val deployment = deploymentService.createDeploymentForService(ExtensionsV1beta1Api(), namespace, user, app)


}
