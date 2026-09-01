package com.websocket.WebsocketProject.config;

import com.websocket.WebsocketProject.security.JWTAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.requestMatchers(
                        // Authentication
                        "/api/v1/user/register",
                        "/api/v1/user/login",
                        // WebSocket
                        "/ws/**",
                        // Health
                        "/health",
                        // Swagger UI
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        // OpenAPI docs
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        // Swagger resources
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api/v1/user/refresh-token",
                        "/uploads/**"
                      ).permitAll().anyRequest().authenticated()
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
