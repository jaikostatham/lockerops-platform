package com.jaico.lockerops.reservation.infrastructure.persistence.repository;

import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from Reservation reservation where reservation.reservationReference = :reference")
    Optional<Reservation> findByReservationReferenceForUpdate(@Param("reference") UUID reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from Reservation reservation where reservation.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    List<Reservation> findByLockerCompartment_Id(Long lockerCompartmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findByStatusAndReservedUntilLessThanEqual(
            ReservationStatus status,
            Instant reservedUntil,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findByStatusAndPaymentExpiresAtLessThanEqual(
            ReservationStatus status,
            Instant paymentExpiresAt,
            Pageable pageable
    );

    boolean existsByLockerCompartment_IdAndStatusIn(
            Long lockerCompartmentId,
            Collection<ReservationStatus> statuses
    );
}
