package com.jaico.lockerops.compartment.infrastructure.persistence.repository;

import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LockerCompartmentRepository extends JpaRepository<LockerCompartment, Long> {

    List<LockerCompartment> findByLockerStation_Id(Long lockerStationId);

    boolean existsByLockerStation_IdAndCompartmentNumber(Long lockerStationId, Integer compartmentNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select compartment from LockerCompartment compartment where compartment.id = :id")
    Optional<LockerCompartment> findByIdForUpdate(@Param("id") Long id);
}
