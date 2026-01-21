package com.gastuapp.infrastructure.security.jwt;

import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 *
 * FLUJO DE DATOS:
 * - INTERCEPTA: Cada HTTP Request antes de llegar al Controller
 * - VALIDA: Token JWT en header "Authorization: Bearer <token>"
 * - ESTABLECE: SecurityContext con usuario autenticado
 * - CONTINÚA: Request hacia el Controller si token válido
 *
 * RESPONSABILIDAD:
 * Filtro de Spring Security que valida tokens JWT en cada request.
 * Extrae información del usuario del token y la establece en el
 * SecurityContext.
 * Permite que los Controllers accedan al usuario autenticado.
 *
 * FLUJO DE EJECUCIÓN:
 * 1. Extraer token del header "Authorization"
 * 2. Validar token con JwtUtils
 * 3. Extraer email del token
 * 4. Buscar usuario en BD
 * 5. Crear Authentication y establecer en SecurityContext
 * 6. Continuar con la cadena de filtros
 *
 * ENDPOINTS AFECTADOS:
 * - Todos los endpoints excepto los públicos definidos en SecurityConfig
 * - Si token inválido/ausente: request continúa sin autenticación
 * - SecurityConfig decide si permite o bloquea el acceso
 *
 * SEGURIDAD:
 * - No arroja excepciones si token inválido (solo no autentica)
 * - Logging de errores para debugging
 * - Validación de usuario activo en BD
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-20
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UsuarioRepositoryPort usuarioRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UsuarioRepositoryPort usuarioRepository) {
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Intercepta cada request y valida el token JWT.
     *
     * FLUJO:
     * 1. Extraer token del header "Authorization: Bearer <token>"
     * 2. Validar token con JwtUtils
     * 3. Extraer email del token
     * 4. Buscar usuario en BD
     * 5. Crear Authentication con rol del usuario
     * 6. Establecer en SecurityContext
     *
     * @param request     HTTP Request
     * @param response    HTTP Response
     * @param filterChain Cadena de filtros
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extraer token del header
            String token = extractTokenFromRequest(request);

            // 2. Si hay token y es válido
            if (token != null && jwtUtils.validateToken(token)) {
                // 3. Extraer datos del token
                String email = jwtUtils.getEmailFromToken(token);
                Long userId = jwtUtils.getUserIdFromToken(token); // NUEVO

                // 4. Buscar usuario en BD (solo para verificar que sigue activo)
                Usuario usuario = usuarioRepository.findById(userId).orElse(null); // CAMBIO: Buscar por ID

                // 5. Si usuario existe y está activo
                if (usuario != null && usuario.getActivo()) {
                    // 6. Crear autenticación con userId como principal
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId.toString(), // CAMBIO: Guardar userId como String
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7. Establecer en SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Usuario autenticado: {} (id: {}) con rol: {}", email, userId, usuario.getRol());
                }
            }
        } catch (Exception e) {
            logger.error("Error al autenticar el usuario: {}", e.getMessage());
        }
        // 8. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header "Authorization".
     *
     * Formato esperado: "Bearer <token>"
     *
     * @param request HTTP Request
     * @return Token JWT o null si no existe/formato inválido
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remover "Bearer "
        }

        return null;
    }
}
