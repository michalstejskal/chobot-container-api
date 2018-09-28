package cz.chobot.container_api.enum

import org.springframework.http.HttpMethod
import javax.swing.text.html.HTML

enum class ModuleOperation(val operation: HttpMethod){
    CREATE(HttpMethod.POST),
    UPDATE(HttpMethod.PUT),
    RESTORE(HttpMethod.PUT),
    DELETE(HttpMethod.DELETE)

}