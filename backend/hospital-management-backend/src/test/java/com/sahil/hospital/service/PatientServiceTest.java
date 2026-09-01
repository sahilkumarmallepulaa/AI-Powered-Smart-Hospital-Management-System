package com.sahil.hospital.service;

import com.sahil.hospital.dto.PatientCreateRequest;
import com.sahil.hospital.dto.PatientResponse;
import com.sahil.hospital.dto.PatientUpdateRequest;
import com.sahil.hospital.entity.BloodGroup;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.PatientGender;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.PatientRepository;
import com.sahil.hospital.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private User user;
    private Patient patient;
    private PatientCreateRequest createRequest;
    private PatientUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(10L)
            .username("patient@example.com")
            .email("patient@example.com")
            .password("encodedPassword123")
            .firstName("Alice")
            .lastName("Smith")
            .phone("5551234567")
            .role(Role.PATIENT)
            .enabled(true)
            .build();

        patient = Patient.builder()
            .id(1L)
            .user(user)
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(PatientGender.FEMALE)
            .bloodGroup(BloodGroup.O_POSITIVE)
            .phoneNumber("5551234567")
            .address("123 Health Ave")
            .emergencyContactName("Bob Smith")
            .emergencyContactPhone("5559876543")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        createRequest = PatientCreateRequest.builder()
            .userId(10L)
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(PatientGender.FEMALE)
            .bloodGroup(BloodGroup.O_POSITIVE)
            .phoneNumber("5551234567")
            .address("123 Health Ave")
            .emergencyContactName("Bob Smith")
            .emergencyContactPhone("5559876543")
            .build();

        updateRequest = PatientUpdateRequest.builder()
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(PatientGender.FEMALE)
            .bloodGroup(BloodGroup.O_POSITIVE)
            .phoneNumber("5550001111")
            .address("456 Wellness Blvd")
            .emergencyContactName("Bob Smith")
            .emergencyContactPhone("5559876543")
            .build();
    }

    @Test
    @DisplayName("1. Create patient successfully")
    void testCreatePatientSuccessfully() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(10L)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse response = patientService.createPatient(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals("Alice", response.getFirstName());
        assertEquals(PatientGender.FEMALE, response.getGender());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("2. User not found when creating patient throws ResourceNotFoundException")
    void testCreatePatientUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        createRequest.setUserId(99L);

        assertThrows(ResourceNotFoundException.class, () -> patientService.createPatient(createRequest));
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Duplicate patient profile for user throws UserAlreadyExistsException")
    void testCreatePatientDuplicateProfileThrowsException() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(10L)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> patientService.createPatient(createRequest));
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. Get patient by ID successfully")
    void testGetPatientByIdSuccess() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getPatientById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alice", response.getFirstName());
    }

    @Test
    @DisplayName("5. Get current patient from SecurityContext successfully")
    void testGetCurrentPatientSuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("patient@example.com");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getCurrentPatient();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("patient@example.com", response.getEmail());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("6. Update patient successfully")
    void testUpdatePatientSuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("patient@example.com");
        when(auth.getAuthorities()).thenAnswer(i -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse response = patientService.updatePatient(1L, updateRequest);

        assertNotNull(response);
        verify(patientRepository, times(1)).save(any(Patient.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("7. PatientResponse does not expose password or sensitive credentials")
    void testPatientResponseDoesNotExposePassword() {
        PatientResponse response = patientService.mapToPatientResponse(patient);

        assertNotNull(response);
        assertEquals("Alice", response.getFirstName());
        assertEquals("patient@example.com", response.getEmail());
        assertFalse(hasProperty(response.getClass(), "password"));
    }

    private boolean hasProperty(Class<?> clazz, String propertyName) {
        try {
            clazz.getDeclaredField(propertyName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
