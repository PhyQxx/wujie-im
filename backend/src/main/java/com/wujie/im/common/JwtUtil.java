package com.wujie.im.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private SecretKey getSigningKey() {
        if (secret == null || secret.isBlank()) {
            log.error("JWT secret is null or blank!");
            throw new IllegalStateException("JWT secret is not configured");
        }
        log.debug("JWT secret length: {}", secret.length());
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, accessTokenValidity);
    }

    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshTokenValidity);
    }

    private String generateToken(Long userId, String username, long validity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.debug("JWT parseToken success: userId={}, username={}, iat={}, exp={}",
                    claims.get("userId"), claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
            return claims;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: iat={}, exp={}", e.getClaims().getIssuedAt(), e.getClaims().getExpiration());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT signature invalid - secret length={}, token prefix={}", secret != null ? secret.length() : "null", token.substring(0, Math.min(50, token.length())));
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT validateToken failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT validateToken unexpected error: {} - {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }
}
