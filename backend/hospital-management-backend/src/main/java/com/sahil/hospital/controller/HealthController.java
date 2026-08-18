package com.sahil.hospital.controller;

import com.sahil.hospital.dto.ApiResponse;
import com.sahil.hospital.dto.HealthStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "System health check endpoint")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check System Health", description = "Returns operational status of the Hospital Management System API backend.")
    public ResponseEntity<ApiResponse<HealthStatusResponse>> checkHealth() {
        HealthStatusResponse healthStatus = HealthStatusResponse.builder()
            .status("UP")
            .application("AI-Powered Smart Hospital Management System")
            .version("1.0.0")
            .build();

        return ResponseEntity.ok(ApiResponse.success("Health check successful", healthStatus));
    }
}
