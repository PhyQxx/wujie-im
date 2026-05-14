package com.wujie.im.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-util-testing-at-least-256-bits");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenValidity", 1000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenValidity", 7000L);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtUtil.generateAccessToken(123L, "testuser");

        assertNotNull(token);
        assertTrue(token.length() > 50);
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtUtil.generateRefreshToken(123L, "testuser");

        assertNotNull(token);
        assertTrue(token.length() > 50);
    }

    @Test
    void parseToken_shouldReturnCorrectClaims() {
        String token = jwtUtil.generateAccessToken(123L, "testuser");

        Claims claims = jwtUtil.parseToken(token);

        assertEquals(123L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void getUserId_shouldReturnUserId() {
        String token = jwtUtil.generateAccessToken(456L, "testuser");

        Long userId = jwtUtil.getUserId(token);

        assertEquals(456L, userId);
    }

    @Test
    void getUsername_shouldReturnUsername() {
        String token = jwtUtil.generateAccessToken(123L, "john_doe");

        String username = jwtUtil.getUsername(token);

        assertEquals("john_doe", username);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateAccessToken(123L, "testuser");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() throws InterruptedException {
        String token = jwtUtil.generateAccessToken(123L, "testuser");

        Thread.sleep(1100);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void parseToken_shouldThrowExpiredJwtExceptionForExpiredToken() throws InterruptedException {
        String token = jwtUtil.generateAccessToken(123L, "testuser");

        Thread.sleep(1100);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    void parseToken_shouldThrowJwtExceptionForTamperedToken() {
        String token = jwtUtil.generateAccessToken(123L, "testuser");
        String tampered = token + "tampered";

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(tampered));
    }

    @Test
    void differentUsers_shouldGenerateDifferentTokens() {
        String token1 = jwtUtil.generateAccessToken(1L, "user1");
        String token2 = jwtUtil.generateAccessToken(2L, "user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void refreshToken_shouldHaveLongerValidityThanAccessToken() {
        String accessToken = jwtUtil.generateAccessToken(123L, "testuser");
        String refreshToken = jwtUtil.generateRefreshToken(123L, "testuser");

        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void getUserId_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.getUserId("invalid.token"));
    }

    @Test
    void getUsername_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.getUsername("invalid.token"));
    }
}
