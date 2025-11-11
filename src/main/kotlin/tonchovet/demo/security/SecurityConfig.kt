package tonchovet.demo.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import tonchovet.demo.repository.UserRepository
import org.springframework.security.web.server.WebFilter
import org.springframework.core.annotation.Order

@Configuration
class SecurityConfig(
    private val jwtProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val jwtAuthFilter = WebFilter { exchange, chain ->
            val header = exchange.request.headers.getFirst("Authorization")
            if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
                chain.filter(exchange)
            } else {
                val token = header.removePrefix("Bearer ")
                val username = jwtProvider.getUsernameFromJwt(token)
                if (username != null) {
                    userRepository.findByUsername(username)
                        .map { user ->
                            UsernamePasswordAuthenticationToken(user.username, null, listOf())
                        }
                        .flatMap { auth ->
                            chain.filter(exchange)
                        }
                        .switchIfEmpty(Mono.error(RuntimeException("User not found")))
                } else {
                    chain.filter(exchange)
                }
            }
        }

        return http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers("/api/auth/**").permitAll()
                .anyExchange().authenticated()
                .and()
            .addFilterAt(jwtAuthFilter, Order.FIRST)
            .build()
    }
}
