package com.sahil.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital.config.SecurityConfig;
import com.sahil.hospital.dto.AppointmentCreateRequest;
import com.sahil.hospital.dto.AppointmentResponse;
import com.sahil.hospital.dto.AppointmentUpdateRequest;
import com.sahil.hospital.entity.AppointmentStatus;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomAccessDeniedHandler;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtAuthenticationEntryPoint;
import com.sahil.hospital.security.JwtAuthenticationFilter;
import com.sahil.hospital.security.JwtService;
import com.sahil.hospital.service.AppointmentService;
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
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationEntryPoint.class,
    CustomAccessDeniedHandler.class,
    JwtService.class,
    JwtAuthenticationFilter.class,
    CustomUserDetailsService.class
})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private UserRepository userRepository;

    private AppointmentCreateRequest createRequest;
    private AppointmentUpdateRequest updateRequest;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        createRequest = AppointmentCreateRequest.builder()
            .doctorId(2L)
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30))
            .reason("General Consultation")
            .build();

        updateRequest = AppointmentUpdateRequest.builder()
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(11, 0))
            .endTime(LocalTime.of(11, 30))
            .reason("Follow-up Consultation")
            .notes("Updated notes")
            .build();

        appointmentResponse = AppointmentResponse.builder()
            .id(100L)
            .patientId(1L)
            .patientName("Jane Doe")
            .doctorId(2L)
            .doctorName("Gregory House")
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.PENDING)
            .reason("General Consultation")
            .build();
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("17. PATIENT can create appointment -> 201 Created")
    void testPatientCanCreateAppointmentSuccess() throws Exception {
        when(appointmentService.createAppointment(any(AppointmentCreateRequest.class))).thenReturn(appointmentResponse);

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("18. Unauthenticated user receives 401 Unauthorized")
    void testUnauthenticatedUserReceives401() throws Exception {
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("19. Non-patient cannot create appointment -> 403 Forbidden")
    void testNonPatientCannotCreateAppointmentForbidden() throws Exception {
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("20. Patient can access own appointments -> 200 OK")
    void testPatientCanAccessOwnAppointments() throws Exception {
        when(appointmentService.getPatientAppointments()).thenReturn(Collections.singletonList(appointmentResponse));

        mockMvc.perform(get("/api/appointments/my")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(100));
    }

    @Test
    @WithMockUser(username = "otherpatient@example.com", roles = {"PATIENT"})
    @DisplayName("21. Patient cannot access another patient's appointment -> 403 Forbidden")
    void testPatientCannotAccessAnotherPatientAppointmentForbidden() throws Exception {
        when(appointmentService.getAppointmentById(100L))
            .thenThrow(new AccessDeniedException("You are not authorized to view this appointment"));

        mockMvc.perform(get("/api/appointments/100")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("22. Doctor can access own appointments -> 200 OK")
    void testDoctorCanAccessOwnAppointments() throws Exception {
        when(appointmentService.getDoctorAppointments()).thenReturn(Collections.singletonList(appointmentResponse));

        mockMvc.perform(get("/api/appointments/doctor/my")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "otherdoctor@example.com", roles = {"DOCTOR"})
    @DisplayName("23. Doctor cannot access another doctor's appointment -> 403 Forbidden")
    void testDoctorCannotAccessAnotherDoctorAppointmentForbidden() throws Exception {
        when(appointmentService.getAppointmentById(100L))
            .thenThrow(new AccessDeniedException("You are not authorized to view this appointment"));

        mockMvc.perform(get("/api/appointments/100")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("24. Doctor can confirm own appointment -> 200 OK")
    void testDoctorCanConfirmOwnAppointmentSuccess() throws Exception {
        appointmentResponse.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentService.confirmAppointment(100L)).thenReturn(appointmentResponse);

        mockMvc.perform(patch("/api/appointments/100/confirm")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "otherdoctor@example.com", roles = {"DOCTOR"})
    @DisplayName("25. Doctor cannot confirm another doctor's appointment -> 403 Forbidden")
    void testDoctorCannotConfirmAnotherDoctorAppointmentForbidden() throws Exception {
        when(appointmentService.confirmAppointment(100L))
            .thenThrow(new AccessDeniedException("Only the assigned doctor or Admin can confirm an appointment"));

        mockMvc.perform(patch("/api/appointments/100/confirm")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("26. ADMIN can manage appointments -> 200 OK")
    void testAdminCanManageAppointmentsSuccess() throws Exception {
        when(appointmentService.getAppointmentById(100L)).thenReturn(appointmentResponse);

        mockMvc.perform(get("/api/appointments/100")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("27. Patient can cancel own appointment -> 200 OK")
    void testPatientCanCancelOwnAppointmentSuccess() throws Exception {
        appointmentResponse.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentService.cancelAppointment(100L)).thenReturn(appointmentResponse);

        mockMvc.perform(patch("/api/appointments/100/cancel")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("28. Patient cannot confirm appointment -> 403 Forbidden")
    void testPatientCannotConfirmAppointmentForbidden() throws Exception {
        mockMvc.perform(patch("/api/appointments/100/confirm")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    @DisplayName("29. Doctor can complete confirmed appointment -> 200 OK")
    void testDoctorCanCompleteConfirmedAppointmentSuccess() throws Exception {
        appointmentResponse.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentService.completeAppointment(100L)).thenReturn(appointmentResponse);

        mockMvc.perform(patch("/api/appointments/100/complete")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = {"PATIENT"})
    @DisplayName("30. Invalid request validation returns 400 Bad Request")
    void testInvalidRequestValidationReturnsBadRequest() throws Exception {
        AppointmentCreateRequest invalidRequest = AppointmentCreateRequest.builder()
            .doctorId(null)
            .appointmentDate(LocalDate.now().minusDays(2))
            .startTime(null)
            .endTime(null)
            .reason("")
            .build();

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
