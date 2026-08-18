package com.sahil.hospital.service;

import com.sahil.hospital.dto.AuthResponse;
import com.sahil.hospital.dto.LoginRequest;
import com.sahil.hospital.dto.RegisterRequest;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("johndoe")
            .email("john@example.com")
            .password("password123")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("1234567890")
            .role(Role.PATIENT)
            .build();

        loginRequest = LoginRequest.builder()
            .usernameOrEmail("johndoe")
            .password("password123")
            .build();

        user = User.builder()
            .id(1L)
            .username("johndoe")
            .email("john@example.com")
            .password("encoded_password")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("1234567890")
            .role(Role.PATIENT)
            .enabled(true)
            .build();

        userDetails = new org.springframework.security.core.userdetails.User(
            "johndoe",
            "encoded_password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );
    }

    @Test
    @DisplayName("Should successfully register a new user with PATIENT role")
    void testRegisterSuccess() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked_jwt_token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getAccessToken());
        assertEquals("johndoe", response.getUsername());
        assertEquals("PATIENT", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when registering duplicate username")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when self-registering as ADMIN")
    void testRegisterSelfAdminDenied() {
        registerRequest.setRole(Role.ADMIN);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login user and return JWT token")
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked_jwt_token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getAccessToken());
        assertEquals("johndoe", response.getUsername());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when login credentials are invalid")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }
}
