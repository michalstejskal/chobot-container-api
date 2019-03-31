package cz.chobot.container_api.config.security

import com.google.common.collect.ImmutableList
import cz.chobot.container_api.service.impl.UserDetailService
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@EnableWebSecurity
open class WebSecurity(private val userDetailService: UserDetailService)//        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    : WebSecurityConfigurerAdapter() {
    private val bCryptPasswordEncoder: BCryptPasswordEncoder? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(JWTAuthenticationFilter(authenticationManager()))
                .addFilter(JWTAuthorizationFilter(authenticationManager()))
                // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Throws(Exception::class)
    public override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.userDetailsService(userDetailService).passwordEncoder(BCryptPasswordEncoder())
    }


    @Bean
    internal open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = ImmutableList.of("*")
        configuration.allowedMethods = ImmutableList.of("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowCredentials = true
        configuration.allowedHeaders = ImmutableList.of("*")
        configuration.exposedHeaders = ImmutableList.of("X-Auth-Token", "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    companion object {

        val SECRET = "SecretKeyToGenJWTsSecretKeyToGenJWTs"
        val EXPIRATION_TIME: Long = 864000000 // 10 days
        val TOKEN_PREFIX = "Bearer "
        val HEADER_STRING = "Authorization"
        val SIGN_UP_URL = "/login"
    }
}