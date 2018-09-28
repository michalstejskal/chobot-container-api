package cz.chobot.container_api.exception

import java.sql.Timestamp

class ErrorDetail(
        val timestamp: Timestamp,
        val code: String
)