package com.jaico.lockerops.reservation.application.service;

import com.jaico.lockerops.compartment.domain.enums.LockerCompartmentStatus;
import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.ticket.application.service.TicketService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ReservationExpirationService {

    private final ReservationRepository reservationRepository;
    private final ReservationExpirationProperties expirationProperties;
    private final TicketService ticketService;

    public ReservationExpirationService(
            ReservationRepository reservationRepository,
            ReservationExpirationProperties expirationProperties,
            TicketService ticketService
    ) {
        this.reservationRepository = reservationRepository;
        this.expirationProperties = expirationProperties;
        this.ticketService = ticketService;
    }

    @Transactional
    public int expireDueReservations() {
        Instant now = Instant.now();
        List<Reservation> dueReservations = reservationRepository
                .findByStatusAndReservedUntilLessThanEqual(
                        ReservationStatus.CONFIRMED,
                        now,
                        PageRequest.of(0, expirationProperties.getBatchSize())
                );
        List<Reservation> unpaidReservations = reservationRepository
                .findByStatusAndPaymentExpiresAtLessThanEqual(
                        ReservationStatus.PENDING_PAYMENT,
                        now,
                        PageRequest.of(0, expirationProperties.getBatchSize())
                );

        int expiredCount = 0;

        for (Reservation reservation : dueReservations) {
            if (expireReservationIfDue(reservation, now)) {
                expiredCount++;
            }
        }

        for (Reservation reservation : unpaidReservations) {
            if (expirePendingPaymentReservation(reservation, now)) {
                expiredCount++;
            }
        }

        return expiredCount;
    }

    private boolean expirePendingPaymentReservation(Reservation reservation, Instant expiredAt) {
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            return false;
        }

        if (reservation.getPaymentExpiresAt() == null
                || reservation.getPaymentExpiresAt().isAfter(expiredAt)) {
            return false;
        }

        reservation.setStatus(ReservationStatus.EXPIRED);
        reservation.setExpiredAt(expiredAt);
        releaseReservedCompartment(reservation.getLockerCompartment());

        return true;
    }

    private boolean expireReservationIfDue(Reservation reservation, Instant expiredAt) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return false;
        }

        if (reservation.getReservedUntil().isAfter(expiredAt)) {
            return false;
        }

        reservation.setStatus(ReservationStatus.EXPIRED);
        reservation.setExpiredAt(expiredAt);
        releaseReservedCompartment(reservation.getLockerCompartment());
        ticketService.expireTicketForReservation(reservation, expiredAt);

        return true;
    }

    private void releaseReservedCompartment(LockerCompartment lockerCompartment) {
        if (lockerCompartment.getStatus() == LockerCompartmentStatus.RESERVED) {
            lockerCompartment.setStatus(LockerCompartmentStatus.AVAILABLE);
        }
    }
}
