package com.sahil.hospital.service;

import com.sahil.hospital.dto.symptom.SymptomAssessmentRequest;
import com.sahil.hospital.dto.symptom.SymptomAssessmentResponse;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.SymptomAssessment;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.PatientRepository;
import com.sahil.hospital.repository.SymptomAssessmentRepository;
import com.sahil.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomAssessmentService {

    private final SymptomAssessmentRepository symptomAssessmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final TriageRuleEngine triageRuleEngine;

    @Transactional
    public SymptomAssessmentResponse createAssessment(SymptomAssessmentRequest request) {
        log.info("Creating symptom assessment for authenticated patient");
        User currentUser = getAuthenticatedUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + currentUser.getId()));

        TriageRuleEngine.TriageResult triageResult = triageRuleEngine.evaluate(request);

        String selectedSymptomsString = null;
        if (request.getSelectedSymptoms() != null && !request.getSelectedSymptoms().isEmpty()) {
            selectedSymptomsString = String.join(", ", request.getSelectedSymptoms());
        }

        SymptomAssessment assessment = SymptomAssessment.builder()
                .patient(patient)
                .primarySymptom(request.getPrimarySymptom())
                .symptomsDescription(request.getSymptomsDescription())
                .selectedSymptoms(selectedSymptomsString)
                .duration(request.getDuration())
                .severity(request.getSeverity())
                .hasCrushingChestPainOrPressure(Boolean.TRUE.equals(request.getHasCrushingChestPainOrPressure()))
                .hasSevereShortnessOfBreath(Boolean.TRUE.equals(request.getHasSevereShortnessOfBreath()))
                .hasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty(
                        Boolean.TRUE.equals(request.getHasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty()))
                .hasSevereUncontrolledBleeding(Boolean.TRUE.equals(request.getHasSevereUncontrolledBleeding()))
                .hasSuddenLossOfConsciousness(Boolean.TRUE.equals(request.getHasSuddenLossOfConsciousness()))
                .urgencyLevel(triageResult.getUrgencyLevel())
                .suggestedSpecialty(triageResult.getSuggestedSpecialty())
                .triageSummary(triageResult.getTriageSummary())
                .safetyGuidance(triageResult.getSafetyGuidance())
                .recommendedAction(triageResult.getRecommendedAction())
                .clinicalSafetyDisclaimer(triageResult.getClinicalSafetyDisclaimer())
                .build();

        SymptomAssessment saved = symptomAssessmentRepository.save(assessment);
        log.info("Symptom assessment created successfully with ID: {} and urgency: {}", saved.getId(), saved.getUrgencyLevel());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SymptomAssessmentResponse getAssessmentById(Long id) {
        log.info("Fetching symptom assessment by ID: {}", id);
        SymptomAssessment assessment = symptomAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Symptom assessment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getAuthenticatedUser();

        if (isUserAdmin(authentication)) {
            return mapToResponse(assessment);
        }

        if (isUserDoctor(authentication)) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

            boolean hasCareRelationship = appointmentRepository.hasActiveCareRelationship(doctor.getId(), assessment.getPatient().getId());
            if (!hasCareRelationship) {
                throw new AccessDeniedException("Access denied: You do not have an active care relationship with this patient.");
            }
            return mapToResponse(assessment);
        }

        if (isAssessmentPatient(authentication, assessment)) {
            return mapToResponse(assessment);
        }

        throw new AccessDeniedException("You are not authorized to view this symptom assessment");
    }

    @Transactional(readOnly = true)
    public List<SymptomAssessmentResponse> getCurrentPatientAssessments() {
        log.info("Fetching symptom assessment history for current authenticated patient");
        User currentUser = getAuthenticatedUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + currentUser.getId()));

        return symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SymptomAssessmentResponse> getPatientAssessments(Long patientId) {
        log.info("Fetching symptom assessment history for patient ID: {}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getAuthenticatedUser();

        if (isUserAdmin(authentication)) {
            return symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (isUserDoctor(authentication)) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

            boolean hasCareRelationship = appointmentRepository.hasActiveCareRelationship(doctor.getId(), patientId);
            if (!hasCareRelationship) {
                throw new AccessDeniedException("Access denied: You do not have an active care relationship with this patient.");
            }
            return symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (isPatientOwner(authentication, patient)) {
            return symptomAssessmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        throw new AccessDeniedException("You are not authorized to view another patient's symptom assessments");
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String principal = authentication.getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal));
    }

    private boolean isUserAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isUserDoctor(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
    }

    private boolean isAssessmentPatient(Authentication authentication, SymptomAssessment assessment) {
        if (authentication == null || assessment == null || assessment.getPatient() == null || assessment.getPatient().getUser() == null) {
            return false;
        }
        String name = authentication.getName();
        User u = assessment.getPatient().getUser();
        return name.equals(u.getEmail()) || name.equals(u.getUsername());
    }

    private boolean isPatientOwner(Authentication authentication, Patient patient) {
        if (authentication == null || patient == null || patient.getUser() == null) {
            return false;
        }
        String name = authentication.getName();
        User u = patient.getUser();
        return name.equals(u.getEmail()) || name.equals(u.getUsername());
    }

    public SymptomAssessmentResponse mapToResponse(SymptomAssessment assessment) {
        Patient p = assessment.getPatient();
        User pu = p != null ? p.getUser() : null;
        String patientName = pu != null ? pu.getFirstName() + " " + pu.getLastName() : "Unknown";

        List<String> selectedSymptomsList = null;
        if (assessment.getSelectedSymptoms() != null && !assessment.getSelectedSymptoms().isBlank()) {
            selectedSymptomsList = Arrays.stream(assessment.getSelectedSymptoms().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        boolean redFlags = assessment.isHasCrushingChestPainOrPressure()
                || assessment.isHasSevereShortnessOfBreath()
                || assessment.isHasSuddenWeaknessOrNumbnessOrFacialDroopOrSpeechDifficulty()
                || assessment.isHasSevereUncontrolledBleeding()
                || assessment.isHasSuddenLossOfConsciousness();

        return SymptomAssessmentResponse.builder()
                .id(assessment.getId())
                .patientId(p != null ? p.getId() : null)
                .patientName(patientName)
                .primarySymptom(assessment.getPrimarySymptom())
                .symptomsDescription(assessment.getSymptomsDescription())
                .selectedSymptoms(selectedSymptomsList)
                .duration(assessment.getDuration())
                .severity(assessment.getSeverity())
                .redFlagsReported(redFlags)
                .urgencyLevel(assessment.getUrgencyLevel())
                .suggestedSpecialty(assessment.getSuggestedSpecialty())
                .triageSummary(assessment.getTriageSummary())
                .safetyGuidance(assessment.getSafetyGuidance())
                .recommendedAction(assessment.getRecommendedAction())
                .clinicalSafetyDisclaimer(assessment.getClinicalSafetyDisclaimer())
                .createdAt(assessment.getCreatedAt())
                .build();
    }
}
