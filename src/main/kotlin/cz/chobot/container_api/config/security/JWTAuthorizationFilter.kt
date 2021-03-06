package cz.chobot.container_api.config.security


import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


open class JWTAuthorizationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {

    /***
     * Check incoming request if contains JWT token headers. If yes check it's validity. Otherwise return unauthorized
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val header = req.getHeader(SecurityUtil.HEADER_STRING)
        //  does request contains token header
        if (header == null || !header.startsWith(SecurityUtil.TOKEN_PREFIX)) {
            //  No -- unauthorized
            chain.doFilter(req, res)
            return
        }

        // yes check its validity
        val authentication = getAuthentication(req)
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }


    /***
     * Checks token validity. If its sign by secret
     */
    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(SecurityUtil.HEADER_STRING)
        if (token != null) {
            // parse the token.
            val claims = Jwts.parser()
                    .setSigningKey(SecurityUtil.SECRET.toByteArray())
                    .parseClaimsJws(token.replace(SecurityUtil.TOKEN_PREFIX, "")).body

            // check user from token
            val user = claims.subject
            return if (user != null) {
                UsernamePasswordAuthenticationToken(user, null, ArrayList<GrantedAuthority>())
            } else null
        }
        return null
    }
}