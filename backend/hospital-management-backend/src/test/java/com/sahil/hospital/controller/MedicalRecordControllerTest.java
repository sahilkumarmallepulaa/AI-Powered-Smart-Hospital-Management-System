package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.MedicalRecordCreateRequest;
import com.sahil.hospital.dto.MedicalRecordResponse;
import com.sahil.hospital.dto.MedicalRecordUpdateRequest;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AppointmentService;
import com.sahil.hospital.service.AuthService;
import com.sahil.hospital.service.DoctorService;
import com.sahil.hospital.service.MedicalRecordService;
import com.sahil.hospital.service.PatientService;
import com.sahil.hospital.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalRecordController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private UserRepository userRepository;

    private MedicalRecordCreateRequest createRequest;
    private MedicalRecordUpdateRequest updateRequest;
    private MedicalRecordResponse recordResponse;

    @BeforeEach
    void setUp() {
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

        recordResponse = MedicalRecordResponse.builder()
            .id(500L)
            .patientId(1L)
            .patientName("Jane Doe")
            .doctorId(2L)
            .doctorName("Gregory House")
            .doctorSpecialization("Diagnostics")
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
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("1. DOCTOR can create medical record -> 201 Created")
    void testDoctorCanCreateMedicalRecordSuccess() throws Exception {
        when(medicalRecordService.createMedicalRecord(any(MedicalRecordCreateRequest.class))).thenReturn(recordResponse);

        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(500))
            .andExpect(jsonPath("$.data.diagnosis").value("Migraine with aura"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("2. PATIENT cannot create medical record -> 403 Forbidden")
    void testPatientCannotCreateMedicalRecordForbidden() throws Exception {
        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Unauthenticated request to create medical record -> 401 Unauthorized")
    void testUnauthenticatedCreateRecordUnauthorized() throws Exception {
        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("4. Patient can view own medical records via /api/medical-records/my -> 200 OK")
    void testPatientCanViewMyRecords() throws Exception {
        when(medicalRecordService.getCurrentPatientMedicalRecords()).thenReturn(Collections.singletonList(recordResponse));

        mockMvc.perform(get("/api/medical-records/my")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(500));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("5. Doctor can view authored records via /api/medical-records/doctor/my -> 200 OK")
    void testDoctorCanViewDoctorMyRecords() throws Exception {
        when(medicalRecordService.getCurrentDoctorMedicalRecords()).thenReturn(Collections.singletonList(recordResponse));

        mockMvc.perform(get("/api/medical-records/doctor/my")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(500));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("6. Authorized patient can get medical record by ID -> 200 OK")
    void testGetMedicalRecordByIdSuccess() throws Exception {
        when(medicalRecordService.getMedicalRecordById(500L)).thenReturn(recordResponse);

        mockMvc.perform(get("/api/medical-records/500")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(500));
    }

    @Test
    @WithMockUser(username = "otherpatient@example.com", roles = {"PATIENT"})
    @DisplayName("7. Patient accessing another patient's medical record by ID -> 403 Forbidden")
    void testGetMedicalRecordByIdForbiddenForOtherPatient() throws Exception {
        when(medicalRecordService.getMedicalRecordById(500L))
            .thenThrow(new AccessDeniedException("You are not authorized to view this medical record"));

        mockMvc.perform(get("/api/medical-records/500")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("8. Doctor can view patient records via /patient/{patientId} -> 200 OK")
    void testDoctorCanViewPatientRecords() throws Exception {
        when(medicalRecordService.getPatientMedicalRecords(1L)).thenReturn(Collections.singletonList(recordResponse));

        mockMvc.perform(get("/api/medical-records/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "otherpatient@example.com", roles = {"PATIENT"})
    @DisplayName("9. Patient accessing another patient's history via /patient/{patientId} -> 403 Forbidden")
    void testPatientCannotViewAnotherPatientRecords() throws Exception {
        when(medicalRecordService.getPatientMedicalRecords(1L))
            .thenThrow(new AccessDeniedException("You are not authorized to view another patient's medical records"));

        mockMvc.perform(get("/api/medical-records/patient/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("10. Author doctor can update medical record -> 200 OK")
    void testAuthorDoctorCanUpdateRecordSuccess() throws Exception {
        when(medicalRecordService.updateMedicalRecord(eq(500L), any(MedicalRecordUpdateRequest.class)))
            .thenReturn(recordResponse);

        mockMvc.perform(put("/api/medical-records/500")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "otherdoctor@example.com", roles = {"DOCTOR"})
    @DisplayName("11. Non-author doctor cannot update medical record -> 403 Forbidden")
    void testNonAuthorDoctorCannotUpdateRecordForbidden() throws Exception {
        when(medicalRecordService.updateMedicalRecord(eq(500L), any(MedicalRecordUpdateRequest.class)))
            .thenThrow(new AccessDeniedException("Only the original recording doctor or Admin can update this medical record"));

        mockMvc.perform(put("/api/medical-records/500")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("12. Patient cannot update medical record -> 403 Forbidden")
    void testPatientCannotUpdateRecordForbidden() throws Exception {
        mockMvc.perform(put("/api/medical-records/500")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("13. Non-existent medical record returns 404 Not Found")
    void testNonExistentRecordReturns404() throws Exception {
        when(medicalRecordService.getMedicalRecordById(999L))
            .thenThrow(new ResourceNotFoundException("Medical record not found with id: 999"));

        mockMvc.perform(get("/api/medical-records/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("14. Validation failure returns 400 Bad Request")
    void testInvalidCreateRequestValidationReturns400() throws Exception {
        MedicalRecordCreateRequest invalidRequest = MedicalRecordCreateRequest.builder()
            .patientId(null)
            .visitDate(LocalDate.now().plusDays(5)) // future date
            .diagnosis("")                          // blank diagnosis
            .heartRate(20)                          // below min 30
            .temperatureCelsius(new BigDecimal("25.0")) // below min 30.0
            .build();

        mockMvc.perform(post("/api/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
