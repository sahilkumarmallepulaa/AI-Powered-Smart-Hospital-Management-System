package com.sahil.hospital.repository;

import com.sahil.hospital.entity.Doctor;
import com.sahil.hospital.entity.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByUserIdAndStatus(Long userId, DoctorStatus status);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByUserId(Long userId);

    List<Doctor> findByStatus(DoctorStatus status);
}
