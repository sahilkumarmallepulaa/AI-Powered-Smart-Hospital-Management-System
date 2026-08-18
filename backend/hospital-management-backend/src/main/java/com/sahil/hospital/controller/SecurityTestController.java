package com.sahil.hospital.controller;

import com.sahil.hospital.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Security Test", description = "Endpoints for testing role-based access control and authorization")
@SecurityRequirement(name = "bearerAuth")
public class SecurityTestController {

    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Authenticated Endpoint", description = "Accessible by any authenticated user regardless of role.")
    public ResponseEntity<ApiResponse<String>> authenticatedAccess() {
        return ResponseEntity.ok(ApiResponse.success("Authenticated access granted", "Authorized"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin Endpoint", description = "Accessible exclusively by users with ADMIN role.")
    public ResponseEntity<ApiResponse<String>> adminAccess() {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", "Authorized"));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Doctor Endpoint", description = "Accessible exclusively by users with DOCTOR role.")
    public ResponseEntity<ApiResponse<String>> doctorAccess() {
        return ResponseEntity.ok(ApiResponse.success("Doctor access granted", "Authorized"));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Patient Endpoint", description = "Accessible exclusively by users with PATIENT role.")
    public ResponseEntity<ApiResponse<String>> patientAccess() {
        return ResponseEntity.ok(ApiResponse.success("Patient access granted", "Authorized"));
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    @Operation(summary = "Receptionist Endpoint", description = "Accessible exclusively by users with RECEPTIONIST role.")
    public ResponseEntity<ApiResponse<String>> receptionistAccess() {
        return ResponseEntity.ok(ApiResponse.success("Receptionist access granted", "Authorized"));
    }
}
