package com.sahil.hospital.service;

import com.sahil.hospital.dto.RegisterRequest;
import com.sahil.hospital.dto.UserResponse;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .password("rawPassword123")
            .phone("9876543210")
            .role(Role.PATIENT)
            .build();

        savedUser = User.builder()
            .id(10L)
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .password("encoded_bcrypt_hash")
            .phone("9876543210")
            .role(Role.PATIENT)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("1. Should successfully register a new user")
    void testSuccessfulUserRegistration() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("encoded_bcrypt_hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());
        assertEquals(Role.PATIENT, response.getRole());
        assertTrue(response.isEnabled());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("2. Should throw BadRequestException when registering duplicate email")
    void testDuplicateEmailRegistrationThrowsException() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.registerUser(registerRequest)
        );

        assertTrue(exception.getMessage().contains("User already exists with email"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("3. Should find user by email successfully")
    void testFindUserByEmailSuccess() {
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(savedUser));

        UserResponse response = userService.findByEmail("jane.doe@example.com");

        assertNotNull(response);
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("3b. Should throw ResourceNotFoundException when email not found")
    void testFindUserByEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.findByEmail("unknown@example.com")
        );
    }

    @Test
    @DisplayName("4. Should find user by id successfully")
    void testFindUserByIdSuccess() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(savedUser));

        UserResponse response = userService.findById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    @DisplayName("4b. Should throw ResourceNotFoundException when id not found")
    void testFindUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.findById(99L)
        );
    }

    @Test
    @DisplayName("5. Should verify password is encoded before saving user to repository")
    void testPasswordIsEncodedBeforeSaving() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("securely_encoded_hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("securely_encoded_hash", capturedUser.getPassword());
        assertNotEquals("rawPassword123", capturedUser.getPassword());
        verify(passwordEncoder, times(1)).encode("rawPassword123");
    }

    @Test
    @DisplayName("6. Should return authenticated user profile from SecurityContext")
    void testGetCurrentUserProfileSuccess() {
        org.springframework.security.core.Authentication authentication =
            mock(org.springframework.security.core.Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("jane.doe@example.com");
        when(authentication.getPrincipal()).thenReturn("jane.doe@example.com");

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(savedUser));

        UserResponse response = userService.getCurrentUserProfile();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals(Role.PATIENT, response.getRole());
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("7. Should throw ResourceNotFoundException when authenticated user does not exist in DB")
    void testGetCurrentUserProfileNotFoundInDb() {
        org.springframework.security.core.Authentication authentication =
            mock(org.springframework.security.core.Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(authentication.getPrincipal()).thenReturn("nonexistent@example.com");

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUserProfile());
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
