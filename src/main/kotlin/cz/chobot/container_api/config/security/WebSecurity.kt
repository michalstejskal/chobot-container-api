package cz.chobot.container_api.config.security

import com.google.common.collect.ImmutableList
import cz.chobot.container_api.config.filter.CORSFilter
import cz.chobot.container_api.service.impl.UserDetailService
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.session.SessionManagementFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.CorsFilter


@EnableWebSecurity
open class WebSecurity(private val userDetailService: UserDetailService)//        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    : WebSecurityConfigurerAdapter() {
    private val bCryptPasswordEncoder: BCryptPasswordEncoder? = null

    @Bean
    open fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("*")
        config.addAllowedHeader("*")
        config.addExposedHeader(HttpHeaders.AUTHORIZATION)
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }


    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, SecurityUtil.SIGN_UP_URL).permitAll()

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
        configuration.allowedMethods = ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowCredentials = true
        configuration.allowedHeaders = ImmutableList.of("*")
        configuration.exposedHeaders = ImmutableList.of("X-Auth-Token", "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
