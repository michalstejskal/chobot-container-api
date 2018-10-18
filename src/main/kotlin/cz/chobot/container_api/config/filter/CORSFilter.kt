package cz.chobot.container_api.config.filter

import org.springframework.stereotype.Component

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException

/***
 * Enable cors headers
 */
@Component
class CORSFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {

        val request = req as HttpServletRequest
        val response = res as HttpServletResponse

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me")

        chain.doFilter(req, res)
    }

    override fun init(filterConfig: FilterConfig) {}

    override fun destroy() {}
}
