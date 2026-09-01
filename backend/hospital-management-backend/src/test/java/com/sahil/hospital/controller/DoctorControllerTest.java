package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.DoctorCreateRequest;
import com.sahil.hospital.dto.DoctorResponse;
import com.sahil.hospital.dto.DoctorUpdateRequest;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AuthService;
import com.sahil.hospital.service.DoctorService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private DoctorCreateRequest createRequest;
    private DoctorUpdateRequest updateRequest;
    private DoctorResponse doctorResponse;

    @BeforeEach
    void setUp() {
        createRequest = DoctorCreateRequest.builder()
            .userId(5L)
            .specialization("Neurology")
            .qualification("MD, DM")
            .licenseNumber("DOC-99999")
            .experienceYears(15)
            .consultationFee(new BigDecimal("200.00"))
            .bio("Expert Neurologist")
            .build();

        updateRequest = DoctorUpdateRequest.builder()
            .specialization("Pediatric Neurology")
            .qualification("MD, DM, PhD")
            .licenseNumber("DOC-99999")
            .experienceYears(17)
            .consultationFee(new BigDecimal("250.00"))
            .bio("Updated Bio")
            .status(DoctorStatus.ACTIVE)
            .build();

        doctorResponse = DoctorResponse.builder()
            .id(1L)
            .userId(5L)
            .firstName("Alan")
            .lastName("Turing")
            .email("alan@example.com")
            .specialization("Neurology")
            .qualification("MD, DM")
            .licenseNumber("DOC-99999")
            .experienceYears(15)
            .consultationFee(new BigDecimal("200.00"))
            .bio("Expert Neurologist")
            .status(DoctorStatus.ACTIVE)
            .build();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("10. ADMIN can create doctor successfully -> 201 Created")
    void testAdminCanCreateDoctorSuccess() throws Exception {
        when(doctorService.createDoctor(any(DoctorCreateRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.licenseNumber").value("DOC-99999"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("11. PATIENT cannot create doctor -> 403 Forbidden")
    void testPatientCannotCreateDoctorForbidden() throws Exception {
        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("12. Authenticated user can view active doctors -> 200 OK")
    void testAuthenticatedUserCanViewDoctors() throws Exception {
        when(doctorService.getAllActiveDoctors()).thenReturn(Collections.singletonList(doctorResponse));

        mockMvc.perform(get("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].specialization").value("Neurology"));
    }

    @Test
    @DisplayName("13. Unauthenticated user cannot access protected doctor endpoints -> 401 Unauthorized")
    void testUnauthenticatedUserCannotAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("14. Doctor can access /api/doctors/me -> 200 OK")
    void testDoctorCanAccessDoctorsMe() throws Exception {
        when(doctorService.getCurrentDoctorProfile()).thenReturn(doctorResponse);

        mockMvc.perform(get("/api/doctors/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("15. PATIENT cannot access /api/doctors/me -> 403 Forbidden")
    void testPatientCannotAccessDoctorsMeForbidden() throws Exception {
        mockMvc.perform(get("/api/doctors/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otherdoctor@example.com", roles = {"DOCTOR"})
    @DisplayName("16. Doctor cannot update another doctor's profile -> 403 Forbidden")
    void testDoctorCannotUpdateAnotherDoctorForbidden() throws Exception {
        when(doctorService.updateDoctor(eq(1L), any(DoctorUpdateRequest.class)))
            .thenThrow(new AccessDeniedException("You are not authorized to update another doctor's profile"));

        mockMvc.perform(put("/api/doctors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("17. ADMIN can update any doctor profile -> 200 OK")
    void testAdminCanUpdateAnyDoctorSuccess() throws Exception {
        when(doctorService.updateDoctor(eq(1L), any(DoctorUpdateRequest.class))).thenReturn(doctorResponse);

        mockMvc.perform(put("/api/doctors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("18. ADMIN can deactivate doctor -> 204 No Content")
    void testAdminCanDeactivateDoctorSuccess() throws Exception {
        doNothing().when(doctorService).deactivateDoctor(1L);

        mockMvc.perform(delete("/api/doctors/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("19. PATIENT cannot deactivate doctor -> 403 Forbidden")
    void testPatientCannotDeactivateDoctorForbidden() throws Exception {
        mockMvc.perform(delete("/api/doctors/1"))
            .andExpect(status().isForbidden());
    }
}
