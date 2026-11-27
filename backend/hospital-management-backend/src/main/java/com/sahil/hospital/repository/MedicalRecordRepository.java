package com.sahil.hospital.repository;

import com.sahil.hospital.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientIdOrderByVisitDateDescCreatedAtDesc(Long patientId);

    List<MedicalRecord> findByDoctorIdOrderByVisitDateDescCreatedAtDesc(Long doctorId);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}
