package cz.chobot.container_api.config.security


import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import cz.chobot.container_api.dto.UserDto
import cz.chobot.container_api.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(private val authManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {

    init {
        this.setFilterProcessesUrl(SecurityUtil.SIGN_UP_URL)
        this.setAuthenticationManager(authManager)
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(req: HttpServletRequest, res: HttpServletResponse?): Authentication {
        try {
            val objectMapper = ObjectMapper()
            objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
            val payload = IOUtils.toString(req.inputStream, Charset.defaultCharset())

            val (username, password) = ObjectMapper().readValue(payload, UserDto::class.java)
            return authManager.authenticate(
                    UsernamePasswordAuthenticationToken(username, password, ArrayList<GrantedAuthority>())
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain?, auth: Authentication) {

        val username = (auth.principal as User).username
        val key = Keys.hmacShaKeyFor(SecurityUtil.SECRET.toByteArray())
        val token = Jwts.builder()
                .setSubject(username)
                .signWith(key)
                .compact()
        res.addHeader(SecurityUtil.HEADER_STRING, SecurityUtil.TOKEN_PREFIX + token)
        res.addHeader("access-control-expose-headers", "Authorization")
    }
}
