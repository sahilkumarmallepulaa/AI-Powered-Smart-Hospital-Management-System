package com.sahil.hospital.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordUpdateRequest {

    @NotNull(message = "Visit date is required")
    @PastOrPresent(message = "Visit date cannot be in the future")
    private LocalDate visitDate;

    @Size(max = 1000, message = "Chief complaint must not exceed 1000 characters")
    private String chiefComplaint;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000, message = "Diagnosis must not exceed 1000 characters")
    private String diagnosis;

    @Size(max = 50, message = "Diagnosis code must not exceed 50 characters")
    private String diagnosisCode;

    @Size(max = 2000, message = "Treatment plan must not exceed 2000 characters")
    private String treatmentPlan;

    @Size(max = 2000, message = "Prescribed medications must not exceed 2000 characters")
    private String prescribedMedications;

    @Size(max = 3000, message = "Consultation notes must not exceed 3000 characters")
    private String consultationNotes;

    @Size(max = 20, message = "Blood pressure must not exceed 20 characters")
    private String bloodPressure;

    @Min(value = 30, message = "Heart rate must be at least 30 bpm")
    @Max(value = 250, message = "Heart rate must not exceed 250 bpm")
    private Integer heartRate;

    @DecimalMin(value = "30.0", message = "Temperature must be at least 30.0 °C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45.0 °C")
    private BigDecimal temperatureCelsius;

    @DecimalMin(value = "0.5", message = "Weight must be at least 0.5 kg")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500.0 kg")
    private BigDecimal weightKg;

    @DecimalMin(value = "20.0", message = "Height must be at least 20.0 cm")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300.0 cm")
    private BigDecimal heightCm;

    @FutureOrPresent(message = "Follow-up date cannot be in the past")
    private LocalDate followUpDate;
}
