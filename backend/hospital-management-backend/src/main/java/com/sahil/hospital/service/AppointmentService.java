package com.sahil.hospital.service;

import com.sahil.hospital.dto.AppointmentCreateRequest;
import com.sahil.hospital.dto.AppointmentResponse;
import com.sahil.hospital.dto.AppointmentUpdateRequest;
import com.sahil.hospital.entity.Appointment;
import com.sahil.hospital.entity.AppointmentStatus;
import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import com.sahil.hospital.entity.Patient;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.ResourceNotFoundException;
import com.sahil.hospital.exception.UserAlreadyExistsException;
import com.sahil.hospital.repository.AppointmentRepository;
import com.sahil.hospital.repository.DoctorRepository;
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
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        log.info("Processing appointment request for doctor ID: {}", request.getDoctorId());

        User currentUser = getAuthenticatedUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + currentUser.getId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new BadRequestException("Doctor is not active and cannot receive appointments");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (appointmentRepository.hasDoctorConflict(doctor.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), null)) {
            throw new UserAlreadyExistsException("Doctor has a conflicting appointment at the selected time slot");
        }

        if (appointmentRepository.hasPatientConflict(patient.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), null)) {
            throw new UserAlreadyExistsException("Patient has a conflicting appointment at the selected time slot");
        }

        Appointment appointment = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .appointmentDate(request.getAppointmentDate())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .status(AppointmentStatus.PENDING)
            .reason(request.getReason())
            .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());

        return mapToAppointmentResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isAppointmentPatient(authentication, appointment) && !isAppointmentDoctor(authentication, appointment)) {
            throw new AccessDeniedException("You are not authorized to view this appointment");
        }

        return mapToAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments() {
        User currentUser = getAuthenticatedUser();
        Patient patient = patientRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + currentUser.getId()));

        return appointmentRepository.findByPatientId(patient.getId()).stream()
            .map(this::mapToAppointmentResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments() {
        User currentUser = getAuthenticatedUser();
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + currentUser.getId()));

        return appointmentRepository.findByDoctorId(doctor.getId()).stream()
            .map(this::mapToAppointmentResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long id) {
        log.info("Confirming appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isAppointmentDoctor(authentication, appointment)) {
            throw new AccessDeniedException("Only the assigned doctor or Admin can confirm an appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Only PENDING appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} confirmed successfully", id);

        return mapToAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse rejectAppointment(Long id) {
        log.info("Rejecting appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isAppointmentDoctor(authentication, appointment)) {
            throw new AccessDeniedException("Only the assigned doctor or Admin can reject an appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Only PENDING appointments can be rejected");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} rejected successfully", id);

        return mapToAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {
        log.info("Cancelling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isAppointmentPatient(authentication, appointment) && !isAppointmentDoctor(authentication, appointment)) {
            throw new AccessDeniedException("You are not authorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} cancelled successfully", id);

        return mapToAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        log.info("Completing appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUserAdmin(authentication) && !isAppointmentDoctor(authentication, appointment)) {
            throw new AccessDeniedException("Only the assigned doctor or Admin can mark an appointment as completed");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED appointments can be marked as COMPLETED");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} marked as COMPLETED", id);

        return mapToAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request) {
        log.info("Updating appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = isUserAdmin(authentication);
        boolean isPatient = isAppointmentPatient(authentication, appointment);
        boolean isDoctor = isAppointmentDoctor(authentication, appointment);

        if (!isAdmin && !isPatient && !isDoctor) {
            throw new AccessDeniedException("You are not authorized to update this appointment");
        }

        if (isPatient && !isAdmin && appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Patients can only update PENDING appointments");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        boolean timeOrDateChanged = !appointment.getAppointmentDate().equals(request.getAppointmentDate())
            || !appointment.getStartTime().equals(request.getStartTime())
            || !appointment.getEndTime().equals(request.getEndTime());

        if (timeOrDateChanged) {
            if (appointmentRepository.hasDoctorConflict(appointment.getDoctor().getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), appointment.getId())) {
                throw new UserAlreadyExistsException("Doctor has a conflicting appointment at the requested time slot");
            }
            if (appointmentRepository.hasPatientConflict(appointment.getPatient().getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), appointment.getId())) {
                throw new UserAlreadyExistsException("Patient has a conflicting appointment at the requested time slot");
            }
        }

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setReason(request.getReason());
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment ID: {} updated successfully", id);

        return mapToAppointmentResponse(updated);
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

    private boolean isAppointmentPatient(Authentication authentication, Appointment appointment) {
        if (authentication == null || appointment == null || appointment.getPatient() == null || appointment.getPatient().getUser() == null) return false;
        String name = authentication.getName();
        User u = appointment.getPatient().getUser();
        return name.equals(u.getEmail()) || name.equals(u.getUsername());
    }

    private boolean isAppointmentDoctor(Authentication authentication, Appointment appointment) {
        if (authentication == null || appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getUser() == null) return false;
        String name = authentication.getName();
        User u = appointment.getDoctor().getUser();
        return name.equals(u.getEmail()) || name.equals(u.getUsername());
    }

    public AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        Patient p = appointment.getPatient();
        Doctor d = appointment.getDoctor();
        User pu = p != null ? p.getUser() : null;
        User du = d != null ? d.getUser() : null;

        String patientName = pu != null ? pu.getFirstName() + " " + pu.getLastName() : "Unknown";
        String doctorName = du != null ? du.getFirstName() + " " + du.getLastName() : "Unknown";

        return AppointmentResponse.builder()
            .id(appointment.getId())
            .patientId(p != null ? p.getId() : null)
            .patientName(patientName)
            .doctorId(d != null ? d.getId() : null)
            .doctorName(doctorName)
            .appointmentDate(appointment.getAppointmentDate())
            .startTime(appointment.getStartTime())
            .endTime(appointment.getEndTime())
            .status(appointment.getStatus())
            .reason(appointment.getReason())
            .notes(appointment.getNotes())
            .createdAt(appointment.getCreatedAt())
            .updatedAt(appointment.getUpdatedAt())
            .build();
    }
}
