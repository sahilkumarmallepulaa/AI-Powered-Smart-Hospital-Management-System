package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.PatientCreateRequest;
import com.sahil.hospital.dto.PatientResponse;
import com.sahil.hospital.dto.PatientUpdateRequest;
import com.sahil.hospital.entity.BloodGroup;
import com.sahil.hospital.entity.PatientGender;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AuthService;
import com.sahil.hospital.service.DoctorService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private UserRepository userRepository;

    private PatientCreateRequest createRequest;
    private PatientUpdateRequest updateRequest;
    private PatientResponse patientResponse;

    @BeforeEach
    void setUp() {
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

        patientResponse = PatientResponse.builder()
            .id(1L)
            .userId(10L)
            .firstName("Alice")
            .lastName("Smith")
            .email("alice@example.com")
            .dateOfBirth(LocalDate.of(1990, 5, 15))
            .gender(PatientGender.FEMALE)
            .bloodGroup(BloodGroup.O_POSITIVE)
            .phoneNumber("5551234567")
            .address("123 Health Ave")
            .emergencyContactName("Bob Smith")
            .emergencyContactPhone("5559876543")
            .build();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("8. ADMIN can create patient successfully -> 201 Created")
    void testAdminCanCreatePatientSuccess() throws Exception {
        when(patientService.createPatient(any(PatientCreateRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("Alice"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("9. PATIENT cannot create another patient -> 403 Forbidden")
    void testPatientCannotCreatePatientForbidden() throws Exception {
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = {"PATIENT"})
    @DisplayName("10. Authenticated patient can access /api/patients/me -> 200 OK")
    void testAuthenticatedPatientCanAccessMe() throws Exception {
        when(patientService.getCurrentPatient()).thenReturn(patientResponse);

        mockMvc.perform(get("/api/patients/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("11. Unauthenticated request to /api/patients/me -> 401 Unauthorized")
    void testUnauthenticatedRequestUnauthorized() throws Exception {
        mockMvc.perform(get("/api/patients/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = {"PATIENT"})
    @DisplayName("13. Patient can update their own profile -> 200 OK")
    void testPatientCanUpdateOwnProfileSuccess() throws Exception {
        when(patientService.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "otherpatient@example.com", roles = {"PATIENT"})
    @DisplayName("14. Patient cannot update another patient's profile -> 403 Forbidden")
    void testPatientCannotUpdateAnotherPatientForbidden() throws Exception {
        when(patientService.updatePatient(eq(1L), any(PatientUpdateRequest.class)))
            .thenThrow(new AccessDeniedException("You are not authorized to update another patient's profile"));

        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("15. ADMIN can update any patient -> 200 OK")
    void testAdminCanUpdateAnyPatientSuccess() throws Exception {
        when(patientService.updatePatient(eq(1L), any(PatientUpdateRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(put("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("16. Non-admin cannot delete patient -> 403 Forbidden")
    void testNonAdminCannotDeletePatientForbidden() throws Exception {
        mockMvc.perform(delete("/api/patients/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("17. ADMIN can delete patient -> 204 No Content")
    void testAdminCanDeletePatientSuccess() throws Exception {
        doNothing().when(patientService).deletePatient(1L);

        mockMvc.perform(delete("/api/patients/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("18. Missing patient produces HTTP 404 Not Found")
    void testMissingPatientNotFound() throws Exception {
        when(patientService.getPatientById(99L))
            .thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(get("/api/patients/99")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("19. Invalid request validation works correctly -> 400 Bad Request")
    void testInvalidRequestValidationFailure() throws Exception {
        PatientCreateRequest invalidRequest = PatientCreateRequest.builder()
            .userId(null)
            .dateOfBirth(LocalDate.now().plusDays(1)) // Future date invalid
            .gender(null)
            .phoneNumber("")
            .address("")
            .build();

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
