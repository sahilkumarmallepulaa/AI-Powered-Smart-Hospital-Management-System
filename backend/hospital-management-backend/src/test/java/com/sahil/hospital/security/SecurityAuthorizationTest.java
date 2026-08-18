package com.sahil.hospital.security;

import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.controller.SecurityTestController;
import com.sahil.hospital.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityTestController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("1. Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    @DisplayName("2. Authenticated ADMIN accessing /api/test/admin should return 200 OK")
    void adminAccessAdminEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Authorized"));
    }

    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    @DisplayName("3. PATIENT accessing /api/test/admin should return 403 Forbidden")
    void patientAccessingAdminEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    @DisplayName("4. DOCTOR accessing /api/test/doctor should return 200 OK")
    void doctorAccessingDoctorEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/test/doctor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Authorized"));
    }

    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    @DisplayName("5. PATIENT accessing /api/test/patient should return 200 OK")
    void patientAccessingPatientEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/test/patient"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Authorized"));
    }

    @Test
    @WithMockUser(username = "receptionistUser", roles = {"RECEPTIONIST"})
    @DisplayName("6. RECEPTIONIST accessing /api/test/receptionist should return 200 OK")
    void receptionistAccessingReceptionistEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/test/receptionist"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Authorized"));
    }

    @Test
    @WithMockUser(username = "anyUser", roles = {"PATIENT"})
    @DisplayName("7. Authenticated user accessing /api/test/authenticated should return 200 OK")
    void authenticatedUserAccessingAuthenticatedEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/test/authenticated"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Authorized"));
    }
}
