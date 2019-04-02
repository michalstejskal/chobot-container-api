package cz.chobot.container_api.enum

/***
 * type of module
 * 0 -- user code from frontend app IDE
 * 1 -- user code from gitlab repository
 * 2 -- copy whole module from Docker image
 */
enum class ModuleType (val code: Int){
    LAMBDA(0),
    REPOSITORY(1),
    IMAGE(2)

}