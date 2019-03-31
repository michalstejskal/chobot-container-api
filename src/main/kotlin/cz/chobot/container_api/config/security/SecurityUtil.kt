package cz.chobot.container_api.config.security

import com.google.common.collect.ImmutableList


open class SecurityUtil {
    companion object {
        val SECRET = "SecretKeyToGenJWTsSecretKeyToGenJWTs"
        val EXPIRATION_TIME: Long = 864000000 // 10 days
        val TOKEN_PREFIX = "Bearer "
        val HEADER_STRING = "Authorization"
        val SIGN_UP_URL = "/login"

        val ALLOWED_METHODS = ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        val EXPOSE_HEADERS = ImmutableList.of("X-Auth-Token", "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
                "Access-Control-Allow-Methods", "Access-Control-Max-Age", "Access-Control-Allow-Headers")
    }
}
