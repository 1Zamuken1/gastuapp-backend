package com.gastuapp.infrastructure.security.jwt;

import com.gastuapp.domain.model.usuario.Usuario;
import com.gastuapp.domain.port.usuario.UsuarioRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter (Supabase Auth)
 *
 * FLUJO DE DATOS:
 * - INTERCEPTA: Cada HTTP Request antes de llegar al Controller
 * - VALIDA: Token JWT de Supabase en header "Authorization: Bearer <token>"
 * - ESTABLECE: SecurityContext con usuario autenticado
 * - CONTINÚA: Request hacia el Controller si token válido
 *
 * ESTRATEGIA DE VALIDACIÓN (Dual):
 * 1. Intenta validar con SupabaseJwtUtils (tokens de Supabase Auth)
 * - Extrae el UUID del usuario del claim 'sub'
 * - Busca el usuario local por supabaseUid
 * 2. Si falla, intenta validar con JwtUtils legado (tokens antiguos)
 * - Mantiene compatibilidad durante la migración
 * - Busca el usuario local por userId (BIGINT)
 *
 * Una vez completada la migración, se puede remover el fallback a JwtUtils.
 *
 * @author Juan Esteban Barrios Portela
 * @version 2.0
 * @since 2026-02-12
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SupabaseJwtUtils supabaseJwtUtils;
    private final JwtUtils jwtUtils; // Legado - mantener durante migración
    private final UsuarioRepositoryPort usuarioRepository;

    public JwtAuthenticationFilter(
            SupabaseJwtUtils supabaseJwtUtils,
            JwtUtils jwtUtils,
            @Lazy UsuarioRepositoryPort usuarioRepository) {
        this.supabaseJwtUtils = supabaseJwtUtils;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Intercepta cada request y valida el token JWT.
     *
     * FLUJO:
     * 1. Extraer token del header "Authorization: Bearer <token>"
     * 2. Intentar validar con SupabaseJwtUtils
     * 3. Si falla, intentar validar con JwtUtils legado
     * 4. Buscar usuario en BD según el tipo de token
     * 5. Crear Authentication con rol del usuario
     * 6. Establecer en SecurityContext
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                logger.info("[AUTH] {} {} - Token recibido (longitud: {})", method, requestURI, token.length());

                // Estrategia 1: Validar como token de Supabase
                boolean supabaseValid = false;
                try {
                    supabaseValid = supabaseJwtUtils.validateToken(token);
                    logger.info("[AUTH] {} {} - Validación Supabase: {}", method, requestURI, supabaseValid);
                } catch (Exception e) {
                    logger.error("[AUTH] {} {} - Error en validación Supabase: {}", method, requestURI, e.getMessage(),
                            e);
                }

                if (supabaseValid) {
                    authenticateWithSupabaseToken(token, request);
                }
                // Estrategia 2: Fallback a validación legada (durante migración)
                else {
                    boolean legacyValid = jwtUtils.validateToken(token);
                    logger.info("[AUTH] {} {} - Validación legada: {}", method, requestURI, legacyValid);
                    if (legacyValid) {
                        authenticateWithLegacyToken(token, request);
                    } else {
                        logger.warn("[AUTH] {} {} - Token NO validado por ninguna estrategia", method, requestURI);
                    }
                }
            } else {
                logger.debug("[AUTH] {} {} - Sin token Authorization", method, requestURI);
            }
        } catch (Exception e) {
            logger.error("[AUTH] {} {} - ERROR GENERAL: {}", method, requestURI, e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Autentica un usuario usando un token de Supabase Auth.
     */
    private void authenticateWithSupabaseToken(String token, HttpServletRequest request) {
        try {
            UUID supabaseUid = supabaseJwtUtils.getSupabaseUid(token);
            String email = supabaseJwtUtils.getEmail(token);
            logger.info("[AUTH-SUPABASE] Buscando usuario con supabase_uid: {} (email: {})", supabaseUid, email);

            Usuario usuario = usuarioRepository.findBySupabaseUid(supabaseUid.toString()).orElse(null);

            if (usuario == null) {
                logger.error("[AUTH-SUPABASE] ❌ Usuario NO encontrado en BD para supabase_uid: {}", supabaseUid);
                return;
            }

            if (!usuario.getActivo()) {
                logger.warn("[AUTH-SUPABASE] ⚠️ Usuario inactivo: {} (supabase_uid: {})", email, supabaseUid);
                return;
            }

            // IMPORTANTE: Usar el ID interno (Long) como principal, NO el supabase_uid
            // (UUID)
            // Los controladores hacen Long.parseLong(authentication.getName()) para obtener
            // el usuarioId
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    usuario.getId().toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("[AUTH-SUPABASE] ✅ Usuario autenticado: {} (supabase_uid: {}, rol: {}, id: {}, principal: {})",
                    email, supabaseUid, usuario.getRol(), usuario.getId(), usuario.getId());
        } catch (Exception e) {
            logger.error("[AUTH-SUPABASE] ❌ Error autenticando token Supabase: {}", e.getMessage(), e);
        }
    }

    /**
     * Autentica un usuario usando un token JWT legado (pre-migración).
     * Este método se mantendrá durante la transición y se eliminará
     * una vez completada la migración a Supabase Auth.
     *
     * @param token   Token JWT legado válido
     * @param request HTTP Request para detalle de autenticación
     */
    private void authenticateWithLegacyToken(String token, HttpServletRequest request) {
        String email = jwtUtils.getEmailFromToken(token);
        Long userId = jwtUtils.getUserIdFromToken(token);

        Usuario usuario = usuarioRepository.findById(userId).orElse(null);

        if (usuario != null && usuario.getActivo()) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Usuario autenticado (legado): {} (id: {}) con rol: {}", email, userId, usuario.getRol());
        }
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
            return bearerToken.substring(7);
        }

        return null;
    }
}
