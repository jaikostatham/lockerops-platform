package com.jaico.lockerops.reservation;

import com.jaico.lockerops.reservation.Reservation;
import com.jaico.lockerops.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationReference(UUID reservationReference);

    List<Reservation> findByLockerCompartment_Id(Long lockerCompartmentId);

    boolean existsByLockerCompartment_IdAndStatusIn(
            Long lockerCompartmentId,
            Collection<ReservationStatus> statuses
    );
}