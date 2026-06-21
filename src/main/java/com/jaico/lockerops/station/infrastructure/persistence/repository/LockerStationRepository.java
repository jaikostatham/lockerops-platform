package com.jaico.lockerops.station.infrastructure.persistence.repository;

import com.jaico.lockerops.station.domain.model.LockerStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerStationRepository extends JpaRepository<LockerStation, Long> {

}
