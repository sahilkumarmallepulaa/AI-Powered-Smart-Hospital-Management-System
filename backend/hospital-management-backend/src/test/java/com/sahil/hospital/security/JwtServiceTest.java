package com.sahil.hospital.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour

        userDetails = new User(
            "testuser",
            "password123",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );
    }

    @Test
    @DisplayName("Should generate a valid JWT token and extract username")
    void testGenerateAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should return true for valid token and matching user details")
    void testIsTokenValid() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false for invalid or corrupted token")
    void testIsTokenValidWithCorruptedToken() {
        boolean isValid = jwtService.isTokenValid("invalid.jwt.token", userDetails);

        assertFalse(isValid);
    }
}
