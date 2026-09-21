package com.example.medicine.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.time.Instant;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/medicines", "/medicines/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/medicines/*/stock/reduce").hasRole("INTERNAL")
                        .requestMatchers(HttpMethod.POST, "/medicines").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/medicines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/medicines/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, exception) -> {
                                                        response.setStatus(401);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":401,\"message\":\"Authentication required\",\"path\":\"" + request.getRequestURI() + "\"}");
                                                })
                                                .accessDeniedHandler((request, response, exception) -> {
                                                        response.setStatus(403);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":403,\"message\":\"Access denied\",\"path\":\"" + request.getRequestURI() + "\"}");
                                                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

        @Bean
        UserDetailsService userDetailsService() {
                return username -> { throw new UsernameNotFoundException(username); };
        }
}
