package com.sahil.hospital.dto;

import com.sahil.hospital.entity.BloodGroup;
import com.sahil.hospital.entity.PatientGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private PatientGender gender;
    private BloodGroup bloodGroup;
    private String phoneNumber;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
