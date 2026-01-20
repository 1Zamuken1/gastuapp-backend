package com.gastuapp.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * Bean de BCryptPasswordEncoder para hashear passwords.
     *
     * SEGURIDAD:
     * - Algoritmo BCrypt con salt aleatorio
     * - Resistente a rainbow tables
     * - Adaptive hashing (costo configurable)
     *
     * USO:
     * - UsuarioMapper: hashear passwords al registrar
     * - AuthService: validar passwords al hacer login
     *
     * @return Instancia de BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}