package com.jaico.lockerops.payment.api.dto.response;

import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;

import java.time.Instant;
import java.util.UUID;

public class SimulatedPaymentResponse {

    private UUID paymentReference;
    private Long reservationId;
    private PaymentStatus paymentStatus;
    private ReservationStatus reservationStatus;
    private Long amountMinor;
    private String currency;
    private Instant processedAt;
    private ReservationTicketResponse ticket;

    public SimulatedPaymentResponse(
            UUID paymentReference,
            Long reservationId,
            PaymentStatus paymentStatus,
            ReservationStatus reservationStatus,
            Long amountMinor,
            String currency,
            Instant processedAt,
            ReservationTicketResponse ticket
    ) {
        this.paymentReference = paymentReference;
        this.reservationId = reservationId;
        this.paymentStatus = paymentStatus;
        this.reservationStatus = reservationStatus;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.processedAt = processedAt;
        this.ticket = ticket;
    }

    public UUID getPaymentReference() {
        return paymentReference;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public Long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public ReservationTicketResponse getTicket() {
        return ticket;
    }
}
