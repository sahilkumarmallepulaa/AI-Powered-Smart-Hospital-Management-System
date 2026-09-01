package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.UserResponse;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AuthService;
import com.sahil.hospital.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("1 & 2. Authenticated user can retrieve GET /api/users/me and correct user is returned")
    void testGetAuthenticatedUserProfileSuccess() throws Exception {
        UserResponse userResponse = UserResponse.builder()
            .id(100L)
            .firstName("John")
            .lastName("Doe")
            .email("patient@example.com")
            .phone("1234567890")
            .role(Role.PATIENT)
            .enabled(true)
            .build();

        when(userService.getCurrentUserProfile()).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.firstName").value("John"))
            .andExpect(jsonPath("$.data.lastName").value("Doe"))
            .andExpect(jsonPath("$.data.email").value("patient@example.com"))
            .andExpect(jsonPath("$.data.role").value("PATIENT"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("3 & 4. Unauthenticated request to GET /api/users/me returns HTTP 401 Unauthorized")
    void testGetProfileUnauthenticatedFails() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Invalid JWT header returns HTTP 401 Unauthorized")
    void testGetProfileWithInvalidJwtFails() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer invalid_jwt_token_payload")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "deleted@example.com", roles = {"PATIENT"})
    @DisplayName("7. Missing user in database produces HTTP 404 Not Found")
    void testGetProfileUserNotFoundInDb() throws Exception {
        when(userService.getCurrentUserProfile())
            .thenThrow(new ResourceNotFoundException("User not found with identifier: deleted@example.com"));

        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
