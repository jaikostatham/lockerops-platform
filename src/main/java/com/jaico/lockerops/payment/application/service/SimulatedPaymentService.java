package com.jaico.lockerops.payment.application.service;

import com.jaico.lockerops.payment.api.dto.request.SimulatePaymentRequest;
import com.jaico.lockerops.payment.api.dto.response.SimulatedPaymentResponse;
import com.jaico.lockerops.payment.application.mapper.PaymentAttemptMapper;
import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import com.jaico.lockerops.payment.domain.model.PaymentAttempt;
import com.jaico.lockerops.payment.infrastructure.persistence.repository.PaymentAttemptRepository;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import com.jaico.lockerops.ticket.application.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class SimulatedPaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TicketService ticketService;
    private final PaymentAttemptMapper paymentAttemptMapper;

    public SimulatedPaymentService(
            ReservationRepository reservationRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            TicketService ticketService,
            PaymentAttemptMapper paymentAttemptMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.ticketService = ticketService;
        this.paymentAttemptMapper = paymentAttemptMapper;
    }

    @Transactional
    public SimulatedPaymentResponse simulatePayment(SimulatePaymentRequest request) {
        Reservation reservation = reservationRepository
                .findByReservationReferenceForUpdate(request.getReservationReference())
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.RESERVATION_NOT_FOUND,
                        request.getReservationReference()
                ));

        validateReservationCanBePaid(reservation);

        Instant processedAt = Instant.now();
        validatePaymentWindowIsOpen(reservation, processedAt);

        PaymentAttempt paymentAttempt = new PaymentAttempt(
                reservation,
                request.getOutcome(),
                reservation.getAmountMinor(),
                reservation.getCurrency(),
                processedAt
        );

        ReservationTicketResponse ticket = null;

        if (request.getOutcome() == PaymentStatus.APPROVED) {
            confirmReservation(reservation, processedAt);
            ticket = ticketService.issueTicketForReservation(reservation);
        }

        PaymentAttempt savedPaymentAttempt = paymentAttemptRepository.save(paymentAttempt);

        return paymentAttemptMapper.toResponse(savedPaymentAttempt, ticket);
    }

    private void validateReservationCanBePaid(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new ApiException(ApiErrorCode.PAYMENT_RESERVATION_NOT_PENDING);
        }
    }

    private void validatePaymentWindowIsOpen(Reservation reservation, Instant now) {
        if (reservation.getPaymentExpiresAt() == null
                || !reservation.getPaymentExpiresAt().isAfter(now)) {
            throw new ApiException(ApiErrorCode.PAYMENT_WINDOW_EXPIRED);
        }
    }

    private void confirmReservation(Reservation reservation, Instant confirmedAt) {
        Duration reservationDuration = Duration.between(
                reservation.getReservedFrom(),
                reservation.getReservedUntil()
        );

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setReservedFrom(confirmedAt);
        reservation.setReservedUntil(confirmedAt.plus(reservationDuration));
    }
}
