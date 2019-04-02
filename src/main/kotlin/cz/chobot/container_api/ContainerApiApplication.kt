package cz.chobot.container_api


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class ContainerApiApplication

fun main(args: Array<String>) {
    runApplication<ContainerApiApplication>(*args)
}
