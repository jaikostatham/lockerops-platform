package com.jaico.lockerops.reservation;

import com.jaico.lockerops.reservation.ReservationResponse;
import com.jaico.lockerops.compartment.LockerCompartment;
import com.jaico.lockerops.reservation.Reservation;
import com.jaico.lockerops.reservation.ReservationStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReservationMapper {

    public Reservation toEntity(
            LockerCompartment lockerCompartment,
            Instant reservedFrom,
            Instant reservedUntil,
            String customerReference
    ) {
        return new Reservation(
                lockerCompartment,
                ReservationStatus.CONFIRMED,
                reservedFrom,
                reservedUntil,
                customerReference
        );
    }

    public ReservationResponse toResponse(Reservation reservation) {
        LockerCompartment lockerCompartment = reservation.getLockerCompartment();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationReference(),
                lockerCompartment.getId(),
                lockerCompartment.getLockerStation().getId(),
                lockerCompartment.getCompartmentNumber(),
                reservation.getStatus(),
                reservation.getReservedFrom(),
                reservation.getReservedUntil(),
                reservation.getCustomerReference(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getCancelledAt(),
                reservation.getExpiredAt(),
                reservation.getCompletedAt()
        );
    }

    public List<ReservationResponse> toResponseList(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }
}