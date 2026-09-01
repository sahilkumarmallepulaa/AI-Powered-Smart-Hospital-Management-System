package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.AuthResponse;
import com.sahil.hospital.dto.LoginRequest;
import com.sahil.hospital.dto.UserResponse;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.exception.UnauthorizedException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.sahil.hospital.service.UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("6. AuthController login endpoint returns HTTP 200 OK for valid credentials")
    void testLoginEndpointSuccess() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
            .email("doctor@example.com")
            .password("password123")
            .build();

        UserResponse userResponse = UserResponse.builder()
            .id(5L)
            .firstName("Doctor")
            .lastName("Smith")
            .email("doctor@example.com")
            .role(Role.DOCTOR)
            .enabled(true)
            .build();

        AuthResponse authResponse = AuthResponse.builder()
            .accessToken("valid_jwt_token_sample")
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .user(userResponse)
            .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("valid_jwt_token_sample"))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.data.user.email").value("doctor@example.com"));
    }

    @Test
    @DisplayName("5a. AuthController returns HTTP 400 Bad Request for invalid request validation")
    void testLoginEndpointValidationFailure() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
            .email("invalid-email-format")
            .password("")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("5b. AuthController returns HTTP 401 Unauthorized for invalid credentials")
    void testLoginEndpointUnauthorized() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
            .email("patient@example.com")
            .password("wrongpassword")
            .build();

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
