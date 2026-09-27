package com.jaico.lockerops.reservation.infrastructure.persistence.repository;

import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationReference(UUID reservationReference);

    List<Reservation> findByLockerCompartment_Id(Long lockerCompartmentId);

    List<Reservation> findByStatusAndReservedUntilLessThanEqual(
            ReservationStatus status,
            Instant reservedUntil,
            Pageable pageable
    );

    boolean existsByLockerCompartment_IdAndStatusIn(
            Long lockerCompartmentId,
            Collection<ReservationStatus> statuses
    );
}
