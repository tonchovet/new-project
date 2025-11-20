package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

import com.example.demo.repository.UserRepository;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * WebFlux Security configuration that adds a JWT authentication filter
 * before the first built‑in filter.
 */
@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    public SecurityConfig(JwtTokenProvider jwtProvider,
                          UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        WebFilter jwtAuthFilter = (exchange, chain) -> {

            String header = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = header.substring(7);               // remove "Bearer "
            String username = jwtProvider.getUsernameFromJwt(token);

            if (username != null) {
                return userRepository.findByUsername(username)
                    .flatMap(user -> {
                        if (jwtProvider.validateToken(token, user.getUsername())) {
                            UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null,
                                    List.of() // Authorities can be set here
                                );
                            return chain.filter(exchange)
                                .contextWrite(ctx -> ctx.put(
                                    org.springframework.security.core.context.ReactiveSecurityContextHolder.
                                        SECURITY_CONTEXT_KEY,
                                    org.springframework.security.core.context.SecurityContextImpl.
                                        fromAuthentication(authToken)
                                ));
                        } else {
                            return chain.filter(exchange);
                        }
                    });
            }

            // no valid token – continue without authentication
            return chain.filter(exchange);
        };

        return http.csrf().disable()
                .authorizeExchange()
                    .pathMatchers("/api/auth/**").permitAll()
                    .anyExchange().authenticated()
                .and()
                .addFilterAt(jwtAuthFilter, Order.FIRST)
                .build();
    }
}
