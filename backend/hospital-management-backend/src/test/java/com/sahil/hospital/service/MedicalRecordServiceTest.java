package com.sahil.hospital.service;

import com.sahil.hospital.dto.MedicalRecordCreateRequest;
import com.sahil.hospital.dto.MedicalRecordResponse;
import com.sahil.hospital.dto.MedicalRecordUpdateRequest;
import com.sahil.hospital.entity.Appointment;
import com.sahil.hospital.entity.AppointmentStatus;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.MedicalRecord;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.MedicalRecordRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private User doctorUser;
    private User otherDoctorUser;
    private User patientUser;
    private User otherPatientUser;
    private Doctor doctor;
    private Doctor otherDoctor;
    private Doctor inactiveDoctor;
    private Patient patient;
    private Patient otherPatient;
    private Appointment appointment;
    private MedicalRecord medicalRecord;
    private MedicalRecordCreateRequest createRequest;
    private MedicalRecordUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        doctorUser = User.builder()
            .id(20L)
            .username("doctor@example.com")
            .email("doctor@example.com")
            .firstName("Gregory")
            .lastName("House")
            .role(Role.DOCTOR)
            .build();

        otherDoctorUser = User.builder()
            .id(21L)
            .username("otherdoctor@example.com")
            .email("otherdoctor@example.com")
            .firstName("James")
            .lastName("Wilson")
            .role(Role.DOCTOR)
            .build();

        patientUser = User.builder()
            .id(10L)
            .username("patient@example.com")
            .email("patient@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .role(Role.PATIENT)
            .build();

        otherPatientUser = User.builder()
            .id(11L)
            .username("otherpatient@example.com")
            .email("otherpatient@example.com")
            .firstName("John")
            .lastName("Smith")
            .role(Role.PATIENT)
            .build();

        doctor = Doctor.builder()
            .id(2L)
            .user(doctorUser)
            .specialization("Diagnostics")
            .status(DoctorStatus.ACTIVE)
            .build();

        otherDoctor = Doctor.builder()
            .id(3L)
            .user(otherDoctorUser)
            .specialization("Oncology")
            .status(DoctorStatus.ACTIVE)
            .build();

        inactiveDoctor = Doctor.builder()
            .id(4L)
            .user(doctorUser)
            .status(DoctorStatus.INACTIVE)
            .build();

        patient = Patient.builder()
            .id(1L)
            .user(patientUser)
            .build();

        otherPatient = Patient.builder()
            .id(2L)
            .user(otherPatientUser)
            .build();

        appointment = Appointment.builder()
            .id(100L)
            .patient(patient)
            .doctor(doctor)
            .appointmentDate(LocalDate.now())
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.CONFIRMED)
            .build();

        medicalRecord = MedicalRecord.builder()
            .id(500L)
            .patient(patient)
            .doctor(doctor)
            .appointment(appointment)
            .visitDate(LocalDate.now())
            .chiefComplaint("Severe headache and nausea")
            .diagnosis("Migraine with aura")
            .diagnosisCode("G43.1")
            .treatmentPlan("Rest in dark room, hydration")
            .prescribedMedications("Sumatriptan 50mg PRN")
            .consultationNotes("Patient reports episodic visual aura before headache onset.")
            .bloodPressure("120/80")
            .heartRate(72)
            .temperatureCelsius(new BigDecimal("36.8"))
            .weightKg(new BigDecimal("68.5"))
            .heightCm(new BigDecimal("172.0"))
            .followUpDate(LocalDate.now().plusWeeks(2))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        createRequest = MedicalRecordCreateRequest.builder()
            .patientId(1L)
            .appointmentId(100L)
            .visitDate(LocalDate.now())
            .chiefComplaint("Severe headache and nausea")
            .diagnosis("Migraine with aura")
            .diagnosisCode("G43.1")
            .treatmentPlan("Rest in dark room, hydration")
            .prescribedMedications("Sumatriptan 50mg PRN")
            .consultationNotes("Patient reports episodic visual aura.")
            .bloodPressure("120/80")
            .heartRate(72)
            .temperatureCelsius(new BigDecimal("36.8"))
            .weightKg(new BigDecimal("68.5"))
            .heightCm(new BigDecimal("172.0"))
            .followUpDate(LocalDate.now().plusWeeks(2))
            .build();

        updateRequest = MedicalRecordUpdateRequest.builder()
            .visitDate(LocalDate.now())
            .chiefComplaint("Headache improved")
            .diagnosis("Migraine controlled")
            .diagnosisCode("G43.1")
            .treatmentPlan("Continue medication as needed")
            .prescribedMedications("Sumatriptan 50mg")
            .consultationNotes("Follow-up evaluation. Symptoms responding well.")
            .bloodPressure("118/76")
            .heartRate(70)
            .temperatureCelsius(new BigDecimal("36.6"))
            .weightKg(new BigDecimal("68.0"))
            .heightCm(new BigDecimal("172.0"))
            .followUpDate(LocalDate.now().plusMonths(1))
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
    @DisplayName("1. Create medical record successfully by doctor")
    void testCreateMedicalRecordSuccessfully() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.existsByAppointmentId(100L)).thenReturn(false);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        MedicalRecordResponse response = medicalRecordService.createMedicalRecord(createRequest);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals(1L, response.getPatientId());
        assertEquals("Jane Doe", response.getPatientName());
        assertEquals(2L, response.getDoctorId());
        assertEquals("Gregory House", response.getDoctorName());
        assertEquals("Migraine with aura", response.getDiagnosis());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("2. Create medical record with non-existent patient throws ResourceNotFoundException")
    void testCreateMedicalRecordPatientNotFound() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        createRequest.setPatientId(99L);
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("3. Inactive doctor cannot create medical record")
    void testCreateMedicalRecordInactiveDoctorThrowsBadRequest() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(inactiveDoctor));

        assertThrows(BadRequestException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("4. Future visit date throws BadRequestException")
    void testCreateMedicalRecordFutureVisitDateThrowsBadRequest() {
        createRequest.setVisitDate(LocalDate.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
    }

    @Test
    @DisplayName("5. Mismatched appointment patient throws BadRequestException")
    void testCreateMedicalRecordMismatchedAppointmentPatient() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Appointment mismatchedAppointment = Appointment.builder()
            .id(100L)
            .patient(otherPatient) // patient 2 instead of 1
            .doctor(doctor)
            .status(AppointmentStatus.CONFIRMED)
            .build();
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(mismatchedAppointment));
        when(medicalRecordRepository.existsByAppointmentId(100L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("6. Mismatched appointment doctor throws BadRequestException")
    void testCreateMedicalRecordMismatchedAppointmentDoctor() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Appointment mismatchedAppointment = Appointment.builder()
            .id(100L)
            .patient(patient)
            .doctor(otherDoctor) // doctor 3 instead of doctor 2
            .status(AppointmentStatus.CONFIRMED)
            .build();
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(mismatchedAppointment));
        when(medicalRecordRepository.existsByAppointmentId(100L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("7. CANCELLED or REJECTED appointment cannot receive medical record")
    void testCreateMedicalRecordCancelledAppointmentThrowsBadRequest() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Appointment cancelledAppointment = Appointment.builder()
            .id(100L)
            .patient(patient)
            .doctor(doctor)
            .status(AppointmentStatus.CANCELLED)
            .build();
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(cancelledAppointment));
        when(medicalRecordRepository.existsByAppointmentId(100L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("8. Duplicate medical record for same appointment throws UserAlreadyExistsException")
    void testCreateMedicalRecordDuplicateAppointmentThrowsConflict() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.existsByAppointmentId(100L)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> medicalRecordService.createMedicalRecord(createRequest));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("9. Get medical record by ID successfully for owner patient")
    void testGetMedicalRecordByIdSuccess() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(medicalRecord));

        MedicalRecordResponse response = medicalRecordService.getMedicalRecordById(500L);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("10. Patient cannot view another patient's medical record by ID -> 403 Forbidden")
    void testGetMedicalRecordByIdForbiddenForOtherPatient() {
        mockAuthentication("otherpatient@example.com", "ROLE_PATIENT");
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(medicalRecord));

        assertThrows(AccessDeniedException.class, () -> medicalRecordService.getMedicalRecordById(500L));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("11. Patient cannot view another patient's medical history by patient ID -> 403 Forbidden")
    void testGetPatientMedicalRecordsForbiddenForOtherPatient() {
        mockAuthentication("otherpatient@example.com", "ROLE_PATIENT");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThrows(AccessDeniedException.class, () -> medicalRecordService.getPatientMedicalRecords(1L));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("12. Patient can view own medical history via /my")
    void testGetCurrentPatientMedicalRecordsSuccess() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(medicalRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(1L))
            .thenReturn(Collections.singletonList(medicalRecord));

        List<MedicalRecordResponse> responses = medicalRecordService.getCurrentPatientMedicalRecords();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(500L, responses.get(0).getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("13. Doctor can view authored medical records via /doctor/my")
    void testGetCurrentDoctorMedicalRecordsSuccess() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.findByDoctorIdOrderByVisitDateDescCreatedAtDesc(2L))
            .thenReturn(Collections.singletonList(medicalRecord));

        List<MedicalRecordResponse> responses = medicalRecordService.getCurrentDoctorMedicalRecords();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(500L, responses.get(0).getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("14. Original doctor can update medical record successfully")
    void testUpdateMedicalRecordSuccessForAuthorDoctor() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        MedicalRecordResponse response = medicalRecordService.updateMedicalRecord(500L, updateRequest);

        assertNotNull(response);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("15. Non-author doctor cannot update medical record -> 403 Forbidden")
    void testUpdateMedicalRecordForbiddenForNonAuthorDoctor() {
        mockAuthentication("otherdoctor@example.com", "ROLE_DOCTOR");
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(medicalRecord));

        assertThrows(AccessDeniedException.class, () -> medicalRecordService.updateMedicalRecord(500L, updateRequest));
        SecurityContextHolder.clearContext();
    }
}
