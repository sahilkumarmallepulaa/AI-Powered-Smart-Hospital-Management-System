package com.sahil.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long appointmentId;
    private LocalDate visitDate;
    private String chiefComplaint;
    private String diagnosis;
    private String diagnosisCode;
    private String treatmentPlan;
    private String prescribedMedications;
    private String consultationNotes;
    private String bloodPressure;
    private Integer heartRate;
    private BigDecimal temperatureCelsius;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private LocalDate followUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
