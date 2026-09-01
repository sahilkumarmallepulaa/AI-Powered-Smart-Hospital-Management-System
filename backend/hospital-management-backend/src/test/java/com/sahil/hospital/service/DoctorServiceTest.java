package com.sahil.hospital.service;

import com.sahil.hospital.dto.DoctorCreateRequest;
import com.sahil.hospital.dto.DoctorResponse;
import com.sahil.hospital.dto.DoctorUpdateRequest;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorService doctorService;

    private User user;
    private Doctor doctor;
    private DoctorCreateRequest createRequest;
    private DoctorUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(5L)
            .username("doctor@example.com")
            .email("doctor@example.com")
            .password("encodedPassword123")
            .firstName("Sarah")
            .lastName("Connor")
            .phone("1234567890")
            .role(Role.DOCTOR)
            .enabled(true)
            .build();

        doctor = Doctor.builder()
            .id(1L)
            .user(user)
            .specialization("Cardiology")
            .qualification("MD, FACC")
            .licenseNumber("DOC-12345")
            .experienceYears(10)
            .consultationFee(new BigDecimal("150.00"))
            .bio("Cardiology specialist")
            .status(DoctorStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        createRequest = DoctorCreateRequest.builder()
            .userId(5L)
            .specialization("Cardiology")
            .qualification("MD, FACC")
            .licenseNumber("DOC-12345")
            .experienceYears(10)
            .consultationFee(new BigDecimal("150.00"))
            .bio("Cardiology specialist")
            .build();

        updateRequest = DoctorUpdateRequest.builder()
            .specialization("Pediatric Cardiology")
            .qualification("MD, FACC, PhD")
            .licenseNumber("DOC-12345")
            .experienceYears(12)
            .consultationFee(new BigDecimal("180.00"))
            .bio("Updated bio")
            .status(DoctorStatus.ACTIVE)
            .build();
    }

    @Test
    @DisplayName("1. Create doctor successfully")
    void testCreateDoctorSuccessfully() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(5L)).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("DOC-12345")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorResponse response = doctorService.createDoctor(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5L, response.getUserId());
        assertEquals("Sarah", response.getFirstName());
        assertEquals("DOC-12345", response.getLicenseNumber());
        assertEquals(DoctorStatus.ACTIVE, response.getStatus());
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    @DisplayName("2. User not found when creating doctor throws ResourceNotFoundException")
    void testCreateDoctorUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        createRequest.setUserId(99L);

        assertThrows(ResourceNotFoundException.class, () -> doctorService.createDoctor(createRequest));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Duplicate doctor for same user throws UserAlreadyExistsException")
    void testCreateDoctorDuplicateUserThrowsException() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(5L)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> doctorService.createDoctor(createRequest));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. Duplicate license number throws UserAlreadyExistsException")
    void testCreateDoctorDuplicateLicenseThrowsException() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(5L)).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("DOC-12345")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> doctorService.createDoctor(createRequest));
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. Get doctor by ID successfully")
    void testGetDoctorByIdSuccess() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Cardiology", response.getSpecialization());
    }

    @Test
    @DisplayName("6. Get all active doctors successfully")
    void testGetAllActiveDoctorsSuccess() {
        when(doctorRepository.findByStatus(DoctorStatus.ACTIVE)).thenReturn(Collections.singletonList(doctor));

        List<DoctorResponse> activeDoctors = doctorService.getAllActiveDoctors();

        assertNotNull(activeDoctors);
        assertEquals(1, activeDoctors.size());
        assertEquals("DOC-12345", activeDoctors.get(0).getLicenseNumber());
    }

    @Test
    @DisplayName("7. Update doctor successfully")
    void testUpdateDoctorSuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("doctor@example.com");
        when(auth.getAuthorities()).thenAnswer(i -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorResponse response = doctorService.updateDoctor(1L, updateRequest);

        assertNotNull(response);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("8. Deactivate doctor sets status to INACTIVE")
    void testDeactivateDoctorSuccess() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        doctorService.deactivateDoctor(1L);

        assertEquals(DoctorStatus.INACTIVE, doctor.getStatus());
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    @DisplayName("9. DoctorResponse does not expose password or sensitive credentials")
    void testDoctorResponseDoesNotExposePassword() {
        DoctorResponse response = doctorService.mapToDoctorResponse(doctor);

        assertNotNull(response);
        assertEquals("Sarah", response.getFirstName());
        assertEquals("doctor@example.com", response.getEmail());
        // Verify DoctorResponse class has no password field by reflection / getters
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
