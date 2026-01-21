package com.gastuapp.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 *
 * UBICACIÓN: Infrastructure Layer - Config
 * RESPONSABILIDAD: Carga propiedades JWT desde application.properties
 *
 * PROPIEDADES CONFIGURABLES:
 * - jwt.secret: Clave secreta para firmar tokens (mínimo 256 bits)
 * - jwt.expiration: Tiempo de vida del token en milisegundos (default: 24
 * horas)
 * - jwt.issuer: Emisor del token (nombre de la aplicación)
 *
 * EJEMPLO EN application.properties:
 * jwt.secret=your-256-bit-secret-key-here-minimum-32-characters-required
 * jwt.expiration=86400000
 * jwt.issuer=GastuApp
 *
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2025-01-19
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clave secreta para firmar JWT.
     * DEBE tener al menos 256 bits (32 caracteres) para HS256.
     *
     * ⚠️ SEGURIDAD:
     * - NUNCA subir a Git (usar variables de entorno en producción)
     * - Cambiar en cada ambiente (dev, staging, prod)
     * - Rotar periódicamente
     */
    private String secret;

    /**
     * Tiempo de vida del token en milisegundos.
     * Default: 86400000ms = 24 horas
     *
     * Ejemplos:
     * - 1 hora: 3600000
     * - 7 días: 604800000
     * - 30 días: 2592000000
     */
    private long expiration = 86400000L; // 24 horas

    /**
     * Emisor del token (claim "iss").
     * Identifica quién generó el token.
     * Útil para validación multi-aplicación.
     */
    private String issuer = "GastuApp";

    // ==================== GETTERS & SETTERS ====================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    // ==================== VALIDACIÓN ====================

    /**
     * Valida que las propiedades JWT estén configuradas correctamente.
     * Se llama automáticamente después de cargar las propiedades.
     *
     * @throws IllegalStateException si la configuración es inválida
     */
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret no puede estar vacío. " +
                            "Configurar 'jwt.secret' en application.properties");
        }

        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret debe tener al menos 32 caracteres (256 bits). " +
                            "Secret actual: " + secret.length() + " caracteres");
        }

        if (expiration <= 0) {
            throw new IllegalStateException(
                    "JWT expiration debe ser mayor a 0. " +
                            "Valor actual: " + expiration);
        }
    }
}