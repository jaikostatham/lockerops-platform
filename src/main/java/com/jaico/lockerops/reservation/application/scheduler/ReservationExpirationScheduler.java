package com.jaico.lockerops.reservation.application.scheduler;

import com.jaico.lockerops.reservation.application.service.ReservationExpirationProperties;
import com.jaico.lockerops.reservation.application.service.ReservationExpirationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {

    private final ReservationExpirationService reservationExpirationService;
    private final ReservationExpirationProperties expirationProperties;

    public ReservationExpirationScheduler(
            ReservationExpirationService reservationExpirationService,
            ReservationExpirationProperties expirationProperties
    ) {
        this.reservationExpirationService = reservationExpirationService;
        this.expirationProperties = expirationProperties;
    }

    @Scheduled(fixedDelayString = "${lockerops.reservations.expiration.fixed-delay-ms:60000}")
    public void expireDueReservations() {
        if (!expirationProperties.isEnabled()) {
            return;
        }

        reservationExpirationService.expireDueReservations();
    }
}
