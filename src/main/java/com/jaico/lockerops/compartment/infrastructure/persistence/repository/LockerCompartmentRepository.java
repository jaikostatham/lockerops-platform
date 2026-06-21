package com.jaico.lockerops.compartment.infrastructure.persistence.repository;

import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LockerCompartmentRepository extends JpaRepository<LockerCompartment, Long> {

    List<LockerCompartment> findByLockerStation_Id(Long lockerStationId);

    boolean existsByLockerStation_IdAndCompartmentNumber(Long lockerStationId, Integer compartmentNumber);
}