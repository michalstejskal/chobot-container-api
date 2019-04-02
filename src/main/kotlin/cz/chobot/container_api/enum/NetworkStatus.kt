package cz.chobot.container_api.enum

/***
 * Status of network -- used in FE and when deployed and undeployed
 */
enum class NetworkStatus(val code: Int){
    CREATED(1),
    SET_TRAIN(2),
    TRAINED(3),
    DEPLOYED(4),
    ERROR(0)
}