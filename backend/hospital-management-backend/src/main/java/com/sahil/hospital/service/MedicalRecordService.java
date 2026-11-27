package com.sahil.hospital.service;

import com.sahil.hospital.dto.MedicalRecordCreateRequest;
import com.sahil.hospital.dto.MedicalRecordResponse;
import com.sahil.hospital.dto.MedicalRecordUpdateRequest;
import com.sahil.hospital.entity.Appointment;
import com.sahil.hospital.entity.AppointmentStatus;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.MedicalRecord;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
import com.sahil.hospital.repository.MedicalRecordRepository;
import com.sahil.hospital.repository.PatientRepository;
import com.sahil.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordCreateRequest request) {
        log.info("Creating medical record for patient ID: {}", request.getPatientId());

        if (request.getVisitDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Visit date cannot be in the future");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = getAuthenticatedUser();
        Doctor doctor = resolveRecordingDoctor(authentication, currentUser, request.getDoctorId());

        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new BadRequestException("Doctor is not active and cannot create medical records");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));

            if (medicalRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
                throw new UserAlreadyExistsException("A medical record already exists for appointment ID: " + request.getAppointmentId());
            }

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new BadRequestException("Appointment does not belong to the specified patient");
            }

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new BadRequestException("Appointment is not assigned to the recording doctor");
            }

            if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.REJECTED) {
                throw new BadRequestException("Cannot create a medical record for a CANCELLED or REJECTED appointment");
            }
        }

        MedicalRecord record = MedicalRecord.builder()
            .patient(patient)
            .doctor(doctor)
            .appointment(appointment)
            .visitDate(request.getVisitDate())
            .chiefComplaint(request.getChiefComplaint())
            .diagnosis(request.getDiagnosis())
            .diagnosisCode(request.getDiagnosisCode())
            .treatmentPlan(request.getTreatmentPlan())
            .prescribedMedications(request.getPrescribedMedications())
            .consultationNotes(request.getConsultationNotes())
            .bloodPressure(request.getBloodPressure())
            .heartRate(request.getHeartRate())
            .temperatureCelsius(request.getTemperatureCelsius())
            .weightKg(request.getWeightKg())
            .heightCm(request.getHeightCm())
            .followUpDate(request.getFollowUpDate())
            .build();

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        log.info("Medical record created successfully with ID: {}", savedRecord.getId());

        return mapToMedicalRecordResponse(savedRecord);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isUserDoctor(authentication) && !isRecordPatient(authentication, record)) {
            throw new AccessDeniedException("You are not authorized to view this medical record");
        }

        return mapToMedicalRecordResponse(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getPatientMedicalRecords(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isUserDoctor(authentication)) {
            if (!isPatientOwner(authentication, patient)) {
                throw new AccessDeniedException("You are not authorized to view another patient's medical records");
            }
        }

        return medicalRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(patientId).stream()
            .map(this::mapToMedicalRecordResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getCurrentPatientMedicalRecords() {
        User currentUser = getAuthenticatedUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + currentUser.getId()));

        return medicalRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(patient.getId()).stream()
            .map(this::mapToMedicalRecordResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getCurrentDoctorMedicalRecords() {
        User currentUser = getAuthenticatedUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + currentUser.getId()));

        return medicalRecordRepository.findByDoctorIdOrderByVisitDateDescCreatedAtDesc(doctor.getId()).stream()
            .map(this::mapToMedicalRecordResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("No medical record found for appointment ID: " + appointmentId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isUserDoctor(authentication) && !isRecordPatient(authentication, record)) {
            throw new AccessDeniedException("You are not authorized to view this medical record");
        }

        return mapToMedicalRecordResponse(record);
    }

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(Long id, MedicalRecordUpdateRequest request) {
        log.info("Updating medical record ID: {}", id);

        if (request.getVisitDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Visit date cannot be in the future");
        }

        MedicalRecord record = medicalRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isRecordAuthorDoctor(authentication, record)) {
            throw new AccessDeniedException("Only the original recording doctor or Admin can update this medical record");
        }

        record.setVisitDate(request.getVisitDate());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setDiagnosis(request.getDiagnosis());
        record.setDiagnosisCode(request.getDiagnosisCode());
        record.setTreatmentPlan(request.getTreatmentPlan());
        record.setPrescribedMedications(request.getPrescribedMedications());
        record.setConsultationNotes(request.getConsultationNotes());
        record.setBloodPressure(request.getBloodPressure());
        record.setHeartRate(request.getHeartRate());
        record.setTemperatureCelsius(request.getTemperatureCelsius());
        record.setWeightKg(request.getWeightKg());
        record.setHeightCm(request.getHeightCm());
        record.setFollowUpDate(request.getFollowUpDate());

        MedicalRecord updated = medicalRecordRepository.save(record);
        log.info("Medical record ID: {} updated successfully", id);

        return mapToMedicalRecordResponse(updated);
    }

    private Doctor resolveRecordingDoctor(Authentication authentication, User currentUser, Long requestedDoctorId) {
        if (isUserAdmin(authentication)) {
            if (requestedDoctorId != null) {
                return doctorRepository.findById(requestedDoctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + requestedDoctorId));
            }
            return doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Doctor ID must be specified when creating a record as ADMIN without a linked doctor profile"));
        }

        return doctorRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for authenticated user"));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User is not authenticated");
        }

        String identifier = authentication.getName();
        return userRepository.findByEmail(identifier)
            .orElseGet(() -> userRepository.findByUsername(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier)));
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

    private boolean isRecordPatient(Authentication authentication, MedicalRecord record) {
        if (authentication == null || record == null || record.getPatient() == null || record.getPatient().getUser() == null) {
            return false;
        }
        String name = authentication.getName();
        User u = record.getPatient().getUser();
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

    private boolean isRecordAuthorDoctor(Authentication authentication, MedicalRecord record) {
        if (authentication == null || record == null || record.getDoctor() == null || record.getDoctor().getUser() == null) {
            return false;
        }
        String name = authentication.getName();
        User u = record.getDoctor().getUser();
        return name.equals(u.getEmail()) || name.equals(u.getUsername());
    }

    public MedicalRecordResponse mapToMedicalRecordResponse(MedicalRecord record) {
        Patient p = record.getPatient();
        Doctor d = record.getDoctor();
        User pu = p != null ? p.getUser() : null;
        User du = d != null ? d.getUser() : null;

        String patientName = pu != null ? pu.getFirstName() + " " + pu.getLastName() : "Unknown";
        String doctorName = du != null ? du.getFirstName() + " " + du.getLastName() : "Unknown";
        String specialization = d != null ? d.getSpecialization() : null;

        return MedicalRecordResponse.builder()
            .id(record.getId())
            .patientId(p != null ? p.getId() : null)
            .patientName(patientName)
            .doctorId(d != null ? d.getId() : null)
            .doctorName(doctorName)
            .doctorSpecialization(specialization)
            .appointmentId(record.getAppointment() != null ? record.getAppointment().getId() : null)
            .visitDate(record.getVisitDate())
            .chiefComplaint(record.getChiefComplaint())
            .diagnosis(record.getDiagnosis())
            .diagnosisCode(record.getDiagnosisCode())
            .treatmentPlan(record.getTreatmentPlan())
            .prescribedMedications(record.getPrescribedMedications())
            .consultationNotes(record.getConsultationNotes())
            .bloodPressure(record.getBloodPressure())
            .heartRate(record.getHeartRate())
            .temperatureCelsius(record.getTemperatureCelsius())
            .weightKg(record.getWeightKg())
            .heightCm(record.getHeightCm())
            .followUpDate(record.getFollowUpDate())
            .createdAt(record.getCreatedAt())
            .updatedAt(record.getUpdatedAt())
            .build();
    }
}
