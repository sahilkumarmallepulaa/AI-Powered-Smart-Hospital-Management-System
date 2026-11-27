package com.sahil.hospital.controller;

import com.sahil.hospital.dto.ApiResponse;
import com.sahil.hospital.dto.MedicalRecordCreateRequest;
import com.sahil.hospital.dto.MedicalRecordResponse;
import com.sahil.hospital.dto.MedicalRecordUpdateRequest;
import com.sahil.hospital.service.MedicalRecordService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Records (EHR)", description = "Endpoints for creating, managing, and viewing electronic health and medical records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create Medical Record", description = "Creates a clinical medical record for a patient. Accessible by DOCTOR or ADMIN.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Medical record created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or clinical data error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Doctor or Admin role required", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient, Doctor, or Appointment not found", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - Duplicate record for appointment", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createMedicalRecord(@Valid @RequestBody MedicalRecordCreateRequest request) {
        MedicalRecordResponse response = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Medical record created successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get My Medical Records (Patient)", description = "Retrieves all medical records for the currently authenticated patient.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical records retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Patient role required", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyMedicalRecords() {
        List<MedicalRecordResponse> response = medicalRecordService.getCurrentPatientMedicalRecords();
        return ResponseEntity.ok(ApiResponse.success("Patient medical records retrieved successfully", response));
    }

    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get My Authored Medical Records (Doctor)", description = "Retrieves all medical records authored by the currently authenticated doctor.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor medical records retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Doctor role required", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor profile not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyDoctorMedicalRecords() {
        List<MedicalRecordResponse> response = medicalRecordService.getCurrentDoctorMedicalRecords();
        return ResponseEntity.ok(ApiResponse.success("Doctor medical records retrieved successfully", response));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('PATIENT')")
    @Operation(summary = "Get Medical Records for Patient", description = "Retrieves medical record history for a patient. Allowed for treating Doctor, Admin, or the Patient themselves.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient medical records retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Unauthorized to view another patient's records", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getPatientMedicalRecords(@PathVariable Long patientId) {
        List<MedicalRecordResponse> response = medicalRecordService.getPatientMedicalRecords(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient medical records retrieved successfully", response));
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get Medical Record by Appointment ID", description = "Retrieves the medical record linked to an appointment.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordByAppointmentId(@PathVariable Long appointmentId) {
        MedicalRecordResponse response = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Medical record retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Medical Record by ID", description = "Retrieves an individual medical record by ID. Authorized for Doctor, Admin, or owner Patient.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordById(@PathVariable Long id) {
        MedicalRecordResponse response = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Medical record retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update Medical Record", description = "Updates a medical record. Accessible only by the original recording Doctor or Admin.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Non-author doctor or unauthorized role", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateMedicalRecord(@PathVariable Long id, @Valid @RequestBody MedicalRecordUpdateRequest request) {
        MedicalRecordResponse response = medicalRecordService.updateMedicalRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully", response));
    }
}
