package com.sahil.hospital.service;

import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.dto.symptom.SymptomAssessmentResponse;
import com.sahil.hospital.entity.AssessmentUrgency;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.SymptomAssessment;
import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.PatientRepository;
import com.sahil.hospital.repository.SymptomAssessmentRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymptomAssessmentServiceTest {

    @Mock
    private SymptomAssessmentRepository symptomAssessmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TriageRuleEngine triageRuleEngine;

    @InjectMocks
    private SymptomAssessmentService symptomAssessmentService;

    private User patientUser;
    private User otherPatientUser;
    private User doctorUser;
    private User adminUser;

    private Patient patient;
    private Patient otherPatient;
    private Doctor doctor;

    private SymptomAssessment assessment;
    private SymptomAssessmentRequest request;

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

        otherPatientUser = User.builder()
                .id(11L)
                .username("otherpatient@example.com")
                .email("otherpatient@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .role(Role.PATIENT)
                .build();

        doctorUser = User.builder()
                .id(20L)
                .username("doctor@example.com")
                .email("doctor@example.com")
                .firstName("John")
                .lastName("Watson")
                .role(Role.DOCTOR)
                .build();

        adminUser = User.builder()
                .id(30L)
                .username("admin@example.com")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("Super")
                .role(Role.ADMIN)
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .build();

        otherPatient = Patient.builder()
                .id(2L)
                .user(otherPatientUser)
                .build();

        doctor = Doctor.builder()
                .id(5L)
                .user(doctorUser)
                .specialization("General Medicine")
                .status(DoctorStatus.ACTIVE)
                .build();

        assessment = SymptomAssessment.builder()
                .id(100L)
                .patient(patient)
                .primarySymptom("Headache")
                .symptomsDescription("Throbbing headache on temples")
                .selectedSymptoms("headache, nausea")
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .hasCrushingChestPainOrPressure(false)
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false)
                .urgencyLevel(AssessmentUrgency.SELF_CARE)
                .suggestedSpecialty("Neurology")
                .triageSummary("Mild symptoms")
                .safetyGuidance("Rest and hydration")
                .recommendedAction("Monitor symptoms")
                .clinicalSafetyDisclaimer(TriageRuleEngine.CLINICAL_SAFETY_DISCLAIMER)
                .createdAt(LocalDateTime.now())
                .build();

        request = SymptomAssessmentRequest.builder()
                .primarySymptom("Headache")
                .symptomsDescription("Throbbing headache on temples")
                .selectedSymptoms(List.of("headache", "nausea"))
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .hasCrushingChestPainOrPressure(false)
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false)
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
    @DisplayName("Create assessment successfully for authenticated patient")
    void testCreateAssessmentSuccessfully() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByUsername("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));

        TriageRuleEngine.TriageResult triageResult = TriageRuleEngine.TriageResult.builder()
                .urgencyLevel(AssessmentUrgency.SELF_CARE)
                .suggestedSpecialty("Neurology")
                .triageSummary("Mild symptoms")
                .safetyGuidance("Rest and hydration")
                .recommendedAction("Monitor symptoms")
                .clinicalSafetyDisclaimer(TriageRuleEngine.CLINICAL_SAFETY_DISCLAIMER)
                .redFlagsReported(false)
                .build();

        when(triageRuleEngine.evaluate(request)).thenReturn(triageResult);
        when(symptomAssessmentRepository.save(any(SymptomAssessment.class))).thenReturn(assessment);

        SymptomAssessmentResponse response = symptomAssessmentService.createAssessment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getPatientId()).isEqualTo(1L);
        assertThat(response.getUrgencyLevel()).isEqualTo(AssessmentUrgency.SELF_CARE);
        assertThat(response.getClinicalSafetyDisclaimer()).contains("informational triage-support tool");
        verify(symptomAssessmentRepository).save(any(SymptomAssessment.class));
    }

    @Test
    @DisplayName("Create assessment throws ResourceNotFoundException when patient profile not found")
    void testCreateAssessmentThrowsWhenPatientProfileNotFound() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByUsername("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> symptomAssessmentService.createAssessment(request));
    }

    @Test
    @DisplayName("Get assessment by ID as ADMIN succeeds")
    void testGetAssessmentByIdAsAdminSucceeds() {
        mockAuthentication("admin@example.com", "ROLE_ADMIN");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("admin@example.com")).thenReturn(Optional.of(adminUser));

        SymptomAssessmentResponse response = symptomAssessmentService.getAssessmentById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get assessment by ID as owning Patient succeeds")
    void testGetAssessmentByIdAsOwningPatientSucceeds() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("patient@example.com")).thenReturn(Optional.of(patientUser));

        SymptomAssessmentResponse response = symptomAssessmentService.getAssessmentById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get assessment by ID as non-owning Patient throws AccessDeniedException")
    void testGetAssessmentByIdAsOtherPatientThrows() {
        mockAuthentication("otherpatient@example.com", "ROLE_PATIENT");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("otherpatient@example.com")).thenReturn(Optional.of(otherPatientUser));

        assertThrows(AccessDeniedException.class, () -> symptomAssessmentService.getAssessmentById(100L));
    }

    @Test
    @DisplayName("Get assessment by ID as Doctor with active care relationship succeeds")
    void testGetAssessmentByIdAsDoctorWithCareRelationshipSucceeds() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasActiveCareRelationship(5L, 1L)).thenReturn(true);

        SymptomAssessmentResponse response = symptomAssessmentService.getAssessmentById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get assessment by ID as Doctor without active care relationship throws AccessDeniedException")
    void testGetAssessmentByIdAsDoctorWithoutCareRelationshipThrows() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasActiveCareRelationship(5L, 1L)).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> symptomAssessmentService.getAssessmentById(100L));
        assertThat(ex.getMessage()).contains("active care relationship");
    }

    @Test
    @DisplayName("Get assessment by ID as Doctor throws generic Doctor profile not found error")
    void testGetAssessmentByIdAsDoctorThrowsGenericErrorWhenProfileMissing() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(symptomAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));
        when(userRepository.findByUsername("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> symptomAssessmentService.getAssessmentById(100L));
        assertThat(ex.getMessage()).isEqualTo("Doctor profile not found");
        assertThat(ex.getMessage()).doesNotContain("doctor@example.com");
    }

    @Test
    @DisplayName("Get assessment by ID throws ResourceNotFoundException when assessment does not exist")
    void testGetAssessmentByIdThrowsWhenNotFound() {
        when(symptomAssessmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> symptomAssessmentService.getAssessmentById(999L));
    }

    @Test
    @DisplayName("Get current patient assessments returns list for authenticated patient")
    void testGetCurrentPatientAssessments() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(userRepository.findByUsername("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(assessment));

        List<SymptomAssessmentResponse> list = symptomAssessmentService.getCurrentPatientAssessments();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get patient assessments by patientId as ADMIN succeeds")
    void testGetPatientAssessmentsAsAdminSucceeds() {
        mockAuthentication("admin@example.com", "ROLE_ADMIN");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(assessment));

        List<SymptomAssessmentResponse> list = symptomAssessmentService.getPatientAssessments(1L);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Get patient assessments by patientId as owning Patient succeeds")
    void testGetPatientAssessmentsAsOwningPatientSucceeds() {
        mockAuthentication("patient@example.com", "ROLE_PATIENT");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("patient@example.com")).thenReturn(Optional.of(patientUser));
        when(symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(assessment));

        List<SymptomAssessmentResponse> list = symptomAssessmentService.getPatientAssessments(1L);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Get patient assessments by patientId as non-owning Patient throws AccessDeniedException")
    void testGetPatientAssessmentsAsNonOwningPatientThrows() {
        mockAuthentication("otherpatient@example.com", "ROLE_PATIENT");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("otherpatient@example.com")).thenReturn(Optional.of(otherPatientUser));

        assertThrows(AccessDeniedException.class, () -> symptomAssessmentService.getPatientAssessments(1L));
    }

    @Test
    @DisplayName("Get patient assessments by patientId as Doctor with active care relationship succeeds")
    void testGetPatientAssessmentsAsDoctorWithCareRelationshipSucceeds() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasActiveCareRelationship(5L, 1L)).thenReturn(true);
        when(symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(assessment));

        List<SymptomAssessmentResponse> list = symptomAssessmentService.getPatientAssessments(1L);

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Get patient assessments by patientId as Doctor without care relationship throws AccessDeniedException")
    void testGetPatientAssessmentsAsDoctorWithoutCareRelationshipThrows() {
        mockAuthentication("doctor@example.com", "ROLE_DOCTOR");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findByUsername("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasActiveCareRelationship(5L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> symptomAssessmentService.getPatientAssessments(1L));
    }
}
