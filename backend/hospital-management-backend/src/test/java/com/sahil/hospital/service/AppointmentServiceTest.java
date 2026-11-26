package com.sahil.hospital.service;

import com.sahil.hospital.dto.AppointmentCreateRequest;
import com.sahil.hospital.dto.AppointmentResponse;
import com.sahil.hospital.dto.AppointmentUpdateRequest;
import com.sahil.hospital.entity.Appointment;
import com.sahil.hospital.entity.AppointmentStatus;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.PatientRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private Doctor activeDoctor;
    private Doctor inactiveDoctor;
    private Appointment appointment;
    private AppointmentCreateRequest createRequest;
    private AppointmentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
            .id(10L)
            .username("patient@example.com")
            .email("patient@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .role(Role.PATIENT)
            .build();

        doctorUser = User.builder()
            .id(20L)
            .username("doctor@example.com")
            .email("doctor@example.com")
            .firstName("Gregory")
            .lastName("House")
            .role(Role.DOCTOR)
            .build();

        patient = Patient.builder()
            .id(1L)
            .user(patientUser)
            .build();

        activeDoctor = Doctor.builder()
            .id(2L)
            .user(doctorUser)
            .status(DoctorStatus.ACTIVE)
            .build();

        inactiveDoctor = Doctor.builder()
            .id(3L)
            .user(doctorUser)
            .status(DoctorStatus.INACTIVE)
            .build();

        appointment = Appointment.builder()
            .id(100L)
            .patient(patient)
            .doctor(activeDoctor)
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.PENDING)
            .reason("Routine Checkup")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        createRequest = AppointmentCreateRequest.builder()
            .doctorId(2L)
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30))
            .reason("Routine Checkup")
            .build();

        updateRequest = AppointmentUpdateRequest.builder()
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(11, 0))
            .endTime(LocalTime.of(11, 30))
            .reason("Follow-up")
            .build();
    }

    private void mockAuthentication(String email, String role) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(email);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getAuthorities()).thenAnswer(i -> Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("1. Create appointment successfully")
    void testCreateAppointmentSuccessfully() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));
        when(appointmentRepository.hasDoctorConflict(eq(2L), any(), any(), any(), isNull())).thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), any(), any(), any(), isNull())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.createAppointment(createRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        assertEquals("Jane Doe", response.getPatientName());
        assertEquals("Gregory House", response.getDoctorName());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("2. Patient profile not found throws ResourceNotFoundException")
    void testCreateAppointmentPatientNotFound() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("3. Doctor not found throws ResourceNotFoundException")
    void testCreateAppointmentDoctorNotFound() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("4. Inactive doctor cannot receive appointment")
    void testCreateAppointmentInactiveDoctorThrowsBadRequest() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        createRequest.setDoctorId(3L);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(3L)).thenReturn(java.util.Optional.of(inactiveDoctor));

        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("5. Past appointment date rejected")
    void testCreateAppointmentPastDateThrowsBadRequest() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        createRequest.setAppointmentDate(LocalDate.now().minusDays(1));
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));

        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("6. Invalid time range (end before start) rejected")
    void testCreateAppointmentInvalidTimeRangeThrowsBadRequest() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        createRequest.setStartTime(LocalTime.of(11, 0));
        createRequest.setEndTime(LocalTime.of(10, 0));
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));

        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("7. Doctor conflict detected")
    void testCreateAppointmentDoctorConflictThrowsUserAlreadyExistsException() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));
        when(appointmentRepository.hasDoctorConflict(eq(2L), any(), any(), any(), isNull())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("8. Patient conflict detected")
    void testCreateAppointmentPatientConflictThrowsUserAlreadyExistsException() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));
        when(appointmentRepository.hasDoctorConflict(eq(2L), any(), any(), any(), isNull())).thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), any(), any(), any(), isNull())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> appointmentService.createAppointment(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("9. Get appointment by ID successfully")
    void testGetAppointmentByIdSuccess() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("10. Update appointment successfully")
    void testUpdateAppointmentSuccess() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.hasDoctorConflict(eq(2L), any(), any(), any(), eq(100L))).thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), any(), any(), any(), eq(100L))).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.updateAppointment(100L, updateRequest);

        assertNotNull(response);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("11. Cancel appointment successfully")
    void testCancelAppointmentSuccess() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.cancelAppointment(100L);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("12. Confirm appointment successfully")
    void testConfirmAppointmentSuccess() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.confirmAppointment(100L);

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("13. Reject appointment successfully")
    void testRejectAppointmentSuccess() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.rejectAppointment(100L);

        assertEquals(AppointmentStatus.REJECTED, appointment.getStatus());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("14. Complete confirmed appointment successfully")
    void testCompleteConfirmedAppointmentSuccess() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.completeAppointment(100L);

        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("15. Invalid state transition (PENDING -> COMPLETED) rejected")
    void testInvalidStateTransitionPendingToCompletedThrowsBadRequest() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        appointment.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(100L)).thenReturn(java.util.Optional.of(appointment));

        assertThrows(BadRequestException.class, () -> appointmentService.completeAppointment(100L));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("16. Cancelled appointment does not block future booking")
    void testCancelledAppointmentNoConflict() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(java.util.Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(java.util.Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(java.util.Optional.of(activeDoctor));
        when(appointmentRepository.hasDoctorConflict(eq(2L), any(), any(), any(), isNull())).thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), any(), any(), any(), isNull())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.createAppointment(createRequest);

        assertNotNull(response);
        verify(appointmentRepository).save(any(Appointment.class));
        SecurityContextHolder.clearContext();
    }
}
