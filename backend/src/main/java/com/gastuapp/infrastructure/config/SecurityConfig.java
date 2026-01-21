package com.gastuapp.infrastructure.config;

import com.gastuapp.infrastructure.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * RESPONSABILIDAD:
 * Configuración de Spring Security para la aplicación.
 * Define qué endpoints son públicos y cuáles requieren autenticación.
 * Configura el filtro JWT para validar tokens en cada request.
 *
 * ENDPOINTS PÚBLICOS:
 * - POST /api/auth/register → Registro de usuarios
 * - POST /api/auth/login → Login de usuarios
 * - GET /api/health → Health check
 *
 * ENDPOINTS PROTEGIDOS:
 * - Todos los demás requieren token JWT válido
 *
 * CONFIGURACIÓN JWT:
 * - Stateless (sin sesiones en servidor)
 * - Token en header "Authorization: Bearer <token>"
 * - Filtro JWT ejecutado antes de cada request
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * CONFIGURACIÓN:
     * - CSRF deshabilitado (API REST stateless)
     * - Sesiones deshabilitadas (JWT stateless)
     * - Endpoints públicos: /auth/**, /health
     * - Filtro JWT añadido antes de UsernamePasswordAuthenticationFilter
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain configurado
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario en API REST con JWT)
                .csrf(csrf -> csrf.disable())

                // Configurar sesiones como STATELESS (sin estado en servidor)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (no requieren autenticación)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/categorias/**").permitAll()

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated())

                // Añadir filtro JWT antes del filtro de autenticación
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
     * @return Instancia de BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}