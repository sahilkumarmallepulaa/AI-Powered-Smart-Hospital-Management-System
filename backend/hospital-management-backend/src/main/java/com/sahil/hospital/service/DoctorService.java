package com.sahil.hospital.service;

import com.sahil.hospital.dto.DoctorCreateRequest;
import com.sahil.hospital.dto.DoctorResponse;
import com.sahil.hospital.dto.DoctorUpdateRequest;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public DoctorResponse createDoctor(DoctorCreateRequest request) {
        log.info("Creating doctor profile for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (doctorRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException("Doctor profile already exists for user ID: " + request.getUserId());
        }

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new UserAlreadyExistsException("Doctor license number already exists: " + request.getLicenseNumber());
        }

        if (user.getRole() != Role.DOCTOR) {
            user.setRole(Role.DOCTOR);
            userRepository.save(user);
        }

        Doctor doctor = Doctor.builder()
            .user(user)
            .specialization(request.getSpecialization())
            .qualification(request.getQualification())
            .licenseNumber(request.getLicenseNumber())
            .experienceYears(request.getExperienceYears())
            .consultationFee(request.getConsultationFee())
            .bio(request.getBio())
            .status(DoctorStatus.ACTIVE)
            .build();

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor profile created successfully with ID: {}", savedDoctor.getId());

        return mapToDoctorResponse(savedDoctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToDoctorResponse(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllActiveDoctors() {
        return doctorRepository.findByStatus(DoctorStatus.ACTIVE).stream()
            .map(this::mapToDoctorResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponse getCurrentDoctorProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User is not authenticated");
        }

        String identifier = authentication.getName();
        User user = userRepository.findByEmail(identifier)
            .orElseGet(() -> userRepository.findByUsername(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + user.getId()));

        return mapToDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request) {
        log.info("Updating doctor profile for ID: {}", id);

        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isCurrentUserAdmin(authentication) && !isDoctorOwner(authentication, doctor)) {
            throw new AccessDeniedException("You are not authorized to update another doctor's profile");
        }

        if (!doctor.getLicenseNumber().equalsIgnoreCase(request.getLicenseNumber()) &&
            doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new UserAlreadyExistsException("Doctor license number already exists: " + request.getLicenseNumber());
        }

        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBio(request.getBio());

        if (request.getStatus() != null) {
            doctor.setStatus(request.getStatus());
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        log.info("Doctor profile updated successfully for ID: {}", updatedDoctor.getId());

        return mapToDoctorResponse(updatedDoctor);
    }

    @Transactional
    public void deactivateDoctor(Long id) {
        log.info("Deactivating doctor with ID: {}", id);

        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctor.setStatus(DoctorStatus.INACTIVE);
        doctorRepository.save(doctor);
        log.info("Doctor ID: {} successfully deactivated (INACTIVE)", id);
    }

    private boolean isCurrentUserAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isDoctorOwner(Authentication authentication, Doctor doctor) {
        if (authentication == null || doctor == null || doctor.getUser() == null) return false;
        String currentUsername = authentication.getName();
        User user = doctor.getUser();
        return currentUsername.equals(user.getEmail()) || currentUsername.equals(user.getUsername());
    }

    public DoctorResponse mapToDoctorResponse(Doctor doctor) {
        User user = doctor.getUser();
        return DoctorResponse.builder()
            .id(doctor.getId())
            .userId(user != null ? user.getId() : null)
            .firstName(user != null ? user.getFirstName() : null)
            .lastName(user != null ? user.getLastName() : null)
            .email(user != null ? user.getEmail() : null)
            .phone(user != null ? user.getPhone() : null)
            .specialization(doctor.getSpecialization())
            .qualification(doctor.getQualification())
            .licenseNumber(doctor.getLicenseNumber())
            .experienceYears(doctor.getExperienceYears())
            .consultationFee(doctor.getConsultationFee())
            .bio(doctor.getBio())
            .status(doctor.getStatus())
            .createdAt(doctor.getCreatedAt())
            .updatedAt(doctor.getUpdatedAt())
            .build();
    }
}
