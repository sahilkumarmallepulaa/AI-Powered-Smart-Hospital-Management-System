package com.sahil.hospital.controller;

import com.sahil.hospital.dto.ApiResponse;
import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.dto.symptom.SymptomAssessmentResponse;
import com.sahil.hospital.service.SymptomAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/symptom-assessments")
@RequiredArgsConstructor
@Tag(name = "AI Symptom Checker", description = "Endpoints for AI-powered clinical triage symptom assessments")
public class SymptomAssessmentController {

    private final SymptomAssessmentService symptomAssessmentService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Create Symptom Assessment", description = "Submits patient symptoms for deterministic clinical triage assessment. Accessible by PATIENT.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Symptom assessment evaluated and saved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or questionnaire error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Patient role required", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<SymptomAssessmentResponse>> createAssessment(
            @Valid @RequestBody SymptomAssessmentRequest request) {
        SymptomAssessmentResponse response = symptomAssessmentService.createAssessment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Symptom assessment evaluated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get My Symptom Assessments", description = "Retrieves the symptom assessment history for the currently authenticated patient.")
    public ResponseEntity<ApiResponse<List<SymptomAssessmentResponse>>> getMyAssessments() {
        List<SymptomAssessmentResponse> response = symptomAssessmentService.getCurrentPatientAssessments();
        return ResponseEntity.ok(ApiResponse.success("Symptom assessments retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get My Symptom Assessments (Alias)", description = "Retrieves the symptom assessment history for the currently authenticated patient.")
    public ResponseEntity<ApiResponse<List<SymptomAssessmentResponse>>> getMyAssessmentsAlias() {
        List<SymptomAssessmentResponse> response = symptomAssessmentService.getCurrentPatientAssessments();
        return ResponseEntity.ok(ApiResponse.success("Symptom assessments retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Get Symptom Assessment by ID", description = "Retrieves an individual symptom assessment record. Accessible by owning patient, authorized care doctor, or ADMIN.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Symptom assessment retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Access denied", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Symptom assessment not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<SymptomAssessmentResponse>> getAssessmentById(@PathVariable Long id) {
        SymptomAssessmentResponse response = symptomAssessmentService.getAssessmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Symptom assessment retrieved successfully", response));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Get Patient Symptom Assessments", description = "Retrieves symptom assessment history for a specific patient. Accessible by patient self, authorized care doctor, or ADMIN.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient symptom assessments retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Access denied", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<SymptomAssessmentResponse>>> getPatientAssessments(@PathVariable Long patientId) {
        List<SymptomAssessmentResponse> response = symptomAssessmentService.getPatientAssessments(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient symptom assessments retrieved successfully", response));
    }
}
