package com.jaico.lockerops.payment.application.mapper;

import com.jaico.lockerops.payment.api.dto.response.SimulatedPaymentResponse;
import com.jaico.lockerops.payment.domain.model.PaymentAttempt;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentAttemptMapper {

    public SimulatedPaymentResponse toResponse(
            PaymentAttempt paymentAttempt,
            ReservationTicketResponse ticket
    ) {
        return new SimulatedPaymentResponse(
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getReservation().getId(),
                paymentAttempt.getStatus(),
                paymentAttempt.getReservation().getStatus(),
                paymentAttempt.getAmountMinor(),
                paymentAttempt.getCurrency(),
                paymentAttempt.getProcessedAt(),
                ticket
        );
    }
}
