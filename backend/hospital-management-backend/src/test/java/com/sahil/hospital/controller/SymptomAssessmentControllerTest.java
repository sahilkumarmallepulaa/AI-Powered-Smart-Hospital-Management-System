package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.dto.symptom.SymptomAssessmentResponse;
import com.sahil.hospital.entity.AssessmentUrgency;
import com.sahil.hospital.entity.SymptomDuration;
import com.sahil.hospital.entity.SymptomSeverity;
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
import com.sahil.hospital.service.SymptomAssessmentService;
import com.sahil.hospital.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SymptomAssessmentController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class SymptomAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SymptomAssessmentService symptomAssessmentService;

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

    private SymptomAssessmentRequest validRequest;
    private SymptomAssessmentResponse response;

    @BeforeEach
    void setUp() {
        validRequest = SymptomAssessmentRequest.builder()
                .primarySymptom("Headache")
                .symptomsDescription("Throbbing pain on temples")
                .selectedSymptoms(List.of("headache", "dizziness"))
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .hasCrushingChestPainOrPressure(false)
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false)
                .build();

        response = SymptomAssessmentResponse.builder()
                .id(1L)
                .patientId(10L)
                .patientName("Jane Doe")
                .primarySymptom("Headache")
                .symptomsDescription("Throbbing pain on temples")
                .selectedSymptoms(List.of("headache", "dizziness"))
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .redFlagsReported(false)
                .urgencyLevel(AssessmentUrgency.SELF_CARE)
                .suggestedSpecialty("Neurology")
                .triageSummary("Mild symptoms")
                .safetyGuidance("Rest and hydration")
                .recommendedAction("Monitor symptoms")
                .clinicalSafetyDisclaimer("This assessment is an informational triage-support tool...")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("Create symptom assessment as PATIENT returns 201 Created")
    void testCreateAssessmentAsPatient() throws Exception {
        when(symptomAssessmentService.createAssessment(any(SymptomAssessmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/symptom-assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.urgencyLevel").value("SELF_CARE"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("Create symptom assessment as DOCTOR returns 403 Forbidden")
    void testCreateAssessmentAsDoctorForbidden() throws Exception {
        mockMvc.perform(post("/api/symptom-assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("Create symptom assessment with missing red-flag question returns 400 Bad Request")
    void testCreateAssessmentValidationFailure() throws Exception {
        SymptomAssessmentRequest invalidRequest = SymptomAssessmentRequest.builder()
                .primarySymptom("Headache")
                .symptomsDescription("Throbbing pain")
                .duration(SymptomDuration.ONE_TO_THREE_DAYS)
                .severity(SymptomSeverity.MILD)
                .hasCrushingChestPainOrPressure(null) // missing mandatory screening field
                .hasSevereShortnessOfBreath(false)
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(false)
                .hasSevereUncontrolledBleeding(false)
                .hasSuddenLossOfConsciousness(false)
                .build();

        mockMvc.perform(post("/api/symptom-assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("Get my symptom assessments as PATIENT returns 200 OK")
    void testGetMyAssessmentsAsPatient() throws Exception {
        when(symptomAssessmentService.getCurrentPatientAssessments()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/symptom-assessments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        mockMvc.perform(get("/api/symptom-assessments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    @DisplayName("Get symptom assessment by ID as PATIENT returns 200 OK")
    void testGetAssessmentById() throws Exception {
        when(symptomAssessmentService.getAssessmentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/symptom-assessments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("Get patient assessments by patientId as DOCTOR returns 200 OK")
    void testGetPatientAssessmentsAsDoctor() throws Exception {
        when(symptomAssessmentService.getPatientAssessments(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/symptom-assessments/patient/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("Unauthenticated request to GET /api/symptom-assessments/1 returns 401 Unauthorized")
    void testUnauthenticatedGetAssessmentReturns401() throws Exception {
        mockMvc.perform(get("/api/symptom-assessments/1"))
                .andExpect(status().isUnauthorized());
    }
}
