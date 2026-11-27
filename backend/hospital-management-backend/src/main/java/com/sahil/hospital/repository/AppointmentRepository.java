package com.sahil.hospital.repository;

import com.sahil.hospital.entity.Appointment;
import com.sahil.hospital.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :appointmentDate AND a.status NOT IN (com.sahil.hospital.entity.AppointmentStatus.CANCELLED, com.sahil.hospital.entity.AppointmentStatus.REJECTED) AND (a.startTime < :endTime AND a.endTime > :startTime) AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean hasDoctorConflict(
        @Param("doctorId") Long doctorId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate = :appointmentDate AND a.status NOT IN (com.sahil.hospital.entity.AppointmentStatus.CANCELLED, com.sahil.hospital.entity.AppointmentStatus.REJECTED) AND (a.startTime < :endTime AND a.endTime > :startTime) AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean hasPatientConflict(
        @Param("patientId") Long patientId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.patient.id = :patientId AND a.status IN (com.sahil.hospital.entity.AppointmentStatus.CONFIRMED, com.sahil.hospital.entity.AppointmentStatus.COMPLETED)")
    boolean hasActiveCareRelationship(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId);
}
