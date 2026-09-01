package com.sahil.hospital.dto;

import com.sahil.hospital.entity.DoctorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private String licenseNumber;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private DoctorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
