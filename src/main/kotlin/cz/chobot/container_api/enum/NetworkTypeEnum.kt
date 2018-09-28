package cz.chobot.container_api.enum

enum class NetworkTypeEnum(val typeName: String){
    IMAGE("image"),
    IMAGE_PRETRAINED("image_preitrained"),
    LOG("log"),
    LOG_PRETRAINED("log_preitrained"),
    CHATBOT("chatbot"),

}