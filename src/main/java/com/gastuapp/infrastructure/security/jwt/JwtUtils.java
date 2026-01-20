package com.gastuapp.infrastructure.security.jwt;

import com.gastuapp.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Utility Class
 *
 * UBICACIÓN: Infrastructure Layer - Security
 * RESPONSABILIDAD: Generación, validación y parseo de tokens JWT
 *
 * FLUJO DE DATOS:
 * - USADO POR: AuthService (Application Layer)
 * - RECIBE: Datos del usuario (email, rol, publicId)
 * - RETORNA: Token JWT firmado
 *
 * ESTRUCTURA DEL TOKEN:
 * Header:
 * {
 *   "alg": "HS256",
 *   "typ": "JWT"
 * }
 *
 * Payload (Claims):
 * {
 *   "sub": "user@example.com",           // Subject: email del usuario
 *   "publicId": "550e8400-...",          // ID público del usuario
 *   "rol": "USER",                       // Rol del usuario
 *   "iat": 1674567890,                   // Issued At: cuándo se generó
 *   "exp": 1674654290,                   // Expiration: cuándo expira
 *   "iss": "GastuApp"                    // Issuer: quién lo generó
 * }
 *
 * Signature:
 * HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
 *
 * SEGURIDAD:
 * - Usa HMAC-SHA256 (HS256) para firma
 * - Secret key de 256 bits mínimo
 * - Validación de expiración
 * - Validación de firma
 * - Logging de errores sin exponer información sensible
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    /**
     * Constructor con inyección de dependencias.
     * Inicializa la clave secreta a partir de las propiedades.
     *
     * @param jwtProperties Propiedades JWT configuradas
     */
    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        // Validar propiedades antes de crear la clave
        jwtProperties.validate();

        // Crear SecretKey a partir del string configurado
        // JJWT 0.12+ requiere SecretKey en lugar de string directo
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        logger.info("JwtUtils inicializado. Expiration: {}ms, Issuer: {}",
                jwtProperties.getExpiration(),
                jwtProperties.getIssuer());
    }

    // ==================== GENERACIÓN DE TOKENS ====================

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * FLUJO:
     * AuthService → login exitoso → [ESTE MÉTODO] → token JWT → cliente
     *
     * CLAIMS INCLUIDOS:
     * - sub (subject): email del usuario
     * - publicId: UUID público del usuario
     * - rol: rol del usuario (USER, ADMIN, USER_HIJO)
     * - iat (issued at): timestamp de creación
     * - exp (expiration): timestamp de expiración
     * - iss (issuer): nombre de la aplicación
     *
     * @param email Email del usuario (subject del token)
     * @param publicId UUID público del usuario
     * @param rol Rol del usuario
     * @return Token JWT firmado
     */
    public String generateToken(String email, String publicId, String rol) {
        // Timestamps
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        // Claims personalizados
        Map<String, Object> claims = new HashMap<>();
        claims.put("publicId", publicId);
        claims.put("rol", rol);

        // Construir y firmar el token
        String token = Jwts.builder()
                .claims(claims)                          // Claims personalizados
                .subject(email)                          // Subject: email del usuario
                .issuedAt(now)                          // Timestamp de creación
                .expiration(expiryDate)                 // Timestamp de expiración
                .issuer(jwtProperties.getIssuer())      // Emisor del token
                .signWith(secretKey)                    // Firma con HS256
                .compact();

        logger.debug("Token generado para usuario: {} (rol: {})", email, rol);

        return token;
    }

    // ==================== VALIDACIÓN DE TOKENS ====================

    /**
     * Valida un token JWT y retorna si es válido.
     *
     * VALIDACIONES:
     * - Firma correcta (secret key válida)
     * - No expirado
     * - Estructura correcta
     *
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)  // Validar firma
                    .build()
                    .parseSignedClaims(token); // Parsear y validar

            return true;

        } catch (SecurityException e) {
            logger.error("JWT signature inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT mal formado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string vacío: {}", e.getMessage());
        }

        return false;
    }

    // ==================== EXTRACCIÓN DE CLAIMS ====================

    /**
     * Extrae el email (subject) del token.
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extrae el publicId del token.
     *
     * @param token Token JWT
     * @return PublicId del usuario (UUID)
     */
    public String getPublicIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("publicId", String.class);
    }

    /**
     * Extrae el rol del usuario del token.
     *
     * @param token Token JWT
     * @return Rol del usuario (USER, ADMIN, USER_HIJO)
     */
    public String getRolFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("rol", String.class);
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT
     * @return true si el token expiró, false si aún es válido
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Extrae todos los claims del token.
     * Método privado usado por los getters públicos.
     *
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== UTILIDADES ====================

    /**
     * Extrae el token del header Authorization.
     * Formato esperado: "Bearer <token>"
     *
     * @param authorizationHeader Header completo
     * @return Token JWT sin el prefijo "Bearer ", o null si el formato es inválido
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remover "Bearer "
        }
        return null;
    }

    /**
     * Genera un token de ejemplo para testing.
     * ⚠️ SOLO PARA DESARROLLO - NO USAR EN PRODUCCIÓN
     */
    public String generateTestToken() {
        return generateToken(
                "test@gastuapp.com",
                "550e8400-e29b-41d4-a716-446655440000",
                "USER"
        );
    }
}