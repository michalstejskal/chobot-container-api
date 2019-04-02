package cz.chobot.container_api.config.security

/***
 * Util class which holds configs for security
 */
open class SecurityUtil {
    companion object {
        const val SECRET = "SecretKeyToGenJWTsSecretKeyToGenJWTs"
        const val TOKEN_PREFIX = "Bearer "
        const val HEADER_STRING = "Authorization"
        const val SIGN_UP_URL = "/login"
    }
}
