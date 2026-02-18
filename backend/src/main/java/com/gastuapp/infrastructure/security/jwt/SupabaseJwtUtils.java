package com.gastuapp.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.interfaces.ECPublicKey;
import java.security.AlgorithmParameters;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilidad para validar JWTs emitidos por Supabase Auth (ES256).
 *
 * FLUJO DE DATOS:
 * - USADO POR: JwtAuthenticationFilter (Infrastructure Layer)
 * - VALIDA: Tokens JWT firmados con ES256 (ECC P-256)
 * - EXTRAE: UUID del usuario (sub claim) de Supabase
 *
 * RESPONSABILIDAD:
 * Valida tokens JWT de Supabase usando claves públicas ES256
 * obtenidas del endpoint JWKS del proyecto Supabase.
 *
 * CONFIGURACIÓN:
 * Requiere la propiedad 'supabase.url' en application.properties.
 * Esta URL se obtiene de: Supabase Dashboard → Settings → API → Project URL.
 *
 * @author Juan Esteban Barrios Portela
 * @version 3.0
 * @since 2026-02-18
 */
@Component
public class SupabaseJwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseJwtUtils.class);

    private final String jwksUrl;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Constructor que configura la URL del JWKS de Supabase.
     *
     * @param supabaseUrl URL del proyecto Supabase (ej: https://xxx.supabase.co)
     */
    public SupabaseJwtUtils(@Value("${supabase.url}") String supabaseUrl) {
        this.jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
        logger.info("SupabaseJwtUtils inicializado con JWKS URL: {}", this.jwksUrl);
        // Pre-cargar las claves al inicio
        try {
            loadJwks();
            logger.info("JWKS cargado exitosamente. {} claves disponibles.", keyCache.size());
        } catch (Exception e) {
            logger.warn("No se pudo pre-cargar JWKS al inicio: {}. Se cargará al validar el primer token.",
                    e.getMessage());
        }
    }

    /**
     * Valida un token JWT de Supabase (ES256).
     *
     * VALIDACIONES:
     * - Firma: Verificada con la clave pública ES256 del JWKS
     * - Expiración: El token no debe estar expirado
     * - Estructura: El token debe tener formato JWT válido
     *
     * @param token Token JWT a validar (sin prefijo "Bearer ")
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            // Extraer el kid del header del token
            String kid = extractKid(token);
            if (kid == null) {
                logger.error("Token JWT sin 'kid' en el header");
                return false;
            }

            // Obtener la clave pública correspondiente
            PublicKey publicKey = getPublicKey(kid);
            if (publicKey == null) {
                logger.error("No se encontró clave pública para kid: {}", kid);
                return false;
            }

            // Validar el token con la clave pública
            Jwts.parser()
                    .verifyWith((ECPublicKey) publicKey)
                    .build()
                    .parseSignedClaims(token);
            logger.debug("Token de Supabase validado exitosamente (ES256)");
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            logger.error("Firma JWT de Supabase inválida: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("Token JWT de Supabase expirado: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Token JWT de Supabase mal formado: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al validar token de Supabase: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrae el UUID del usuario (sub claim) del token de Supabase.
     *
     * @param token Token JWT válido de Supabase
     * @return UUID del usuario de Supabase
     */
    public UUID getSupabaseUid(String token) {
        Claims claims = getClaims(token);
        String sub = claims.getSubject();
        logger.debug("Supabase UID extraído: {}", sub);
        return UUID.fromString(sub);
    }

    /**
     * Extrae el email del usuario del token de Supabase.
     *
     * @param token Token JWT válido de Supabase
     * @return Email del usuario o null si no existe
     */
    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extrae el rol del token de Supabase.
     *
     * @param token Token JWT válido de Supabase
     * @return Rol del token (ej: "authenticated", "anon")
     */
    public String getTokenRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Extrae el 'kid' (Key ID) del header del token JWT.
     * Se usa para identificar cuál clave pública del JWKS usar.
     */
    private String extractKid(String token) {
        try {
            String header = token.split("\\.")[0];
            byte[] decoded = Base64.getUrlDecoder().decode(header);
            JsonNode headerJson = objectMapper.readTree(decoded);
            return headerJson.has("kid") ? headerJson.get("kid").asText() : null;
        } catch (Exception e) {
            logger.error("Error extrayendo kid del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la clave pública para un kid dado.
     * Primero busca en cache, si no la encuentra, recarga el JWKS.
     */
    private PublicKey getPublicKey(String kid) {
        // Buscar en cache
        PublicKey key = keyCache.get(kid);
        if (key != null) {
            return key;
        }

        // Recargar JWKS (puede haber rotación de claves)
        try {
            loadJwks();
            return keyCache.get(kid);
        } catch (Exception e) {
            logger.error("Error recargando JWKS: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Carga las claves públicas desde el endpoint JWKS de Supabase.
     */
    private void loadJwks() throws Exception {
        logger.debug("Cargando JWKS desde: {}", jwksUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jwksUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al obtener JWKS. Status: " + response.statusCode()
                    + " Body: " + response.body());
        }

        JsonNode jwks = objectMapper.readTree(response.body());
        JsonNode keys = jwks.get("keys");

        if (keys != null && keys.isArray()) {
            for (JsonNode keyNode : keys) {
                String kid = keyNode.get("kid").asText();
                String kty = keyNode.get("kty").asText();

                if ("EC".equals(kty)) {
                    PublicKey publicKey = buildECPublicKey(keyNode);
                    keyCache.put(kid, publicKey);
                    logger.info("Clave pública EC cargada para kid: {}", kid);
                }
            }
            logger.info("Total claves JWKS cargadas: {}", keyCache.size());
        } else {
            logger.warn("Respuesta JWKS sin claves válidas: {}", response.body());
        }
    }

    /**
     * Construye una clave pública EC a partir de los parámetros JWK.
     */
    private PublicKey buildECPublicKey(JsonNode keyNode) throws Exception {
        String x = keyNode.get("x").asText();
        String y = keyNode.get("y").asText();

        // Decodificar coordenadas X e Y (Base64url)
        byte[] xBytes = Base64.getUrlDecoder().decode(x);
        byte[] yBytes = Base64.getUrlDecoder().decode(y);

        BigInteger xCoord = new BigInteger(1, xBytes);
        BigInteger yCoord = new BigInteger(1, yBytes);

        // Crear parámetros de curva P-256
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
        params.init(new java.security.spec.ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

        // Crear la clave pública
        ECPoint point = new ECPoint(xCoord, yCoord);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");

        return keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * Extrae todos los claims del token usando la clave pública ES256.
     */
    private Claims getClaims(String token) {
        String kid = extractKid(token);
        PublicKey publicKey = getPublicKey(kid);

        return Jwts.parser()
                .verifyWith((ECPublicKey) publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
