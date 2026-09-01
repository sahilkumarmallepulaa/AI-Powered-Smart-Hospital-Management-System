package com.sahil.hospital.service;

import com.sahil.hospital.dto.PatientCreateRequest;
import com.sahil.hospital.dto.PatientResponse;
import com.sahil.hospital.dto.PatientUpdateRequest;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.PatientRepository;
import com.sahil.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional
    public PatientResponse createPatient(PatientCreateRequest request) {
        log.info("Creating patient profile for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (patientRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException("Patient profile already exists for user ID: " + request.getUserId());
        }

        if (user.getRole() == null) {
            user.setRole(Role.PATIENT);
            userRepository.save(user);
        }

        Patient patient = Patient.builder()
            .user(user)
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .bloodGroup(request.getBloodGroup())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .emergencyContactName(request.getEmergencyContactName())
            .emergencyContactPhone(request.getEmergencyContactPhone())
            .build();

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient profile created successfully with ID: {}", savedPatient.getId());

        return mapToPatientResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToPatientResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getCurrentPatient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User is not authenticated");
        }

        String identifier = authentication.getName();
        User user = userRepository.findByEmail(identifier)
            .orElseGet(() -> userRepository.findByUsername(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));

        Patient patient = patientRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + user.getId()));

        return mapToPatientResponse(patient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientUpdateRequest request) {
        log.info("Updating patient profile for ID: {}", id);

        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isCurrentUserAdmin(authentication) && !isPatientOwner(authentication, patient)) {
            throw new AccessDeniedException("You are not authorized to update another patient's profile");
        }

        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient profile updated successfully for ID: {}", updatedPatient.getId());

        return mapToPatientResponse(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient profile ID: {}", id);

        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patientRepository.delete(patient);
        log.info("Patient ID: {} successfully deleted", id);
    }

    private boolean isCurrentUserAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isPatientOwner(Authentication authentication, Patient patient) {
        if (authentication == null || patient == null || patient.getUser() == null) return false;
        String currentUsername = authentication.getName();
        User user = patient.getUser();
        return currentUsername.equals(user.getEmail()) || currentUsername.equals(user.getUsername());
    }

    public PatientResponse mapToPatientResponse(Patient patient) {
        User user = patient.getUser();
        return PatientResponse.builder()
            .id(patient.getId())
            .userId(user != null ? user.getId() : null)
            .firstName(user != null ? user.getFirstName() : null)
            .lastName(user != null ? user.getLastName() : null)
            .email(user != null ? user.getEmail() : null)
            .dateOfBirth(patient.getDateOfBirth())
            .gender(patient.getGender())
            .bloodGroup(patient.getBloodGroup())
            .phoneNumber(patient.getPhoneNumber())
            .address(patient.getAddress())
            .emergencyContactName(patient.getEmergencyContactName())
            .emergencyContactPhone(patient.getEmergencyContactPhone())
            .createdAt(patient.getCreatedAt())
            .updatedAt(patient.getUpdatedAt())
            .build();
    }
}
