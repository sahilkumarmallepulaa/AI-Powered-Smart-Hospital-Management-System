package com.sahil.hospital.service;

import com.sahil.hospital.dto.AuthResponse;
import com.sahil.hospital.dto.LoginRequest;
import com.sahil.hospital.dto.UserResponse;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.UnauthorizedException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;
    private User disabledUser;
    private UserDetails userDetails;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
            .email("john@example.com")
            .password("password123")
            .build();

        user = User.builder()
            .id(1L)
            .username("john@example.com")
            .email("john@example.com")
            .password("encoded_password")
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .role(Role.PATIENT)
            .enabled(true)
            .build();

        disabledUser = User.builder()
            .id(2L)
            .email("disabled@example.com")
            .password("encoded_password")
            .firstName("Disabled")
            .lastName("User")
            .role(Role.PATIENT)
            .enabled(false)
            .build();

        userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "encoded_password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        userResponse = UserResponse.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .phone("1234567890")
            .role(Role.PATIENT)
            .enabled(true)
            .build();
    }

    @Test
    @DisplayName("1. Successful login should verify password, generate JWT, and return AuthResponse")
    void testSuccessfulLogin() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked_jwt_token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);
        when(userService.mapToUserResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("john@example.com", response.getUser().getEmail());
        verify(passwordEncoder, times(1)).matches("password123", "encoded_password");
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("2. Login with invalid password should be rejected with UnauthorizedException")
    void testLoginWithInvalidPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encoded_password")).thenReturn(false);

        loginRequest.setPassword("wrongPassword");

        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("3. Login with unknown email should be rejected with UnauthorizedException")
    void testLoginWithUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        loginRequest.setEmail("unknown@example.com");

        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("4. Login attempt by a disabled user should be rejected with UnauthorizedException")
    void testLoginWithDisabledUser() {
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(disabledUser));

        loginRequest.setEmail("disabled@example.com");

        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("User account is disabled", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }
}
