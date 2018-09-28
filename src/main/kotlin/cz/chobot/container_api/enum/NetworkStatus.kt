package cz.chobot.container_api.enum

enum class NetworkStatus(val code: Int){
    CREATED(1),
    SET_TRAIN(2),
    TRAINED(3),
    DEPLOYED(4),
    ERROR(0)

    // 1 created
    // 2 set train data
    // 3 training finished
    // 4 deployed
}