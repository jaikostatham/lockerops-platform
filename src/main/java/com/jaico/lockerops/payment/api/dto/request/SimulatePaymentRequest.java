package com.jaico.lockerops.payment.api.dto.request;

import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SimulatePaymentRequest {

    @NotNull(message = "El identificador de la reserva es obligatorio")
    @Positive(message = "El identificador de la reserva debe ser mayor que cero")
    private Long reservationId;

    @NotNull(message = "El resultado simulado del pago es obligatorio")
    private PaymentStatus outcome;

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public PaymentStatus getOutcome() {
        return outcome;
    }

    public void setOutcome(PaymentStatus outcome) {
        this.outcome = outcome;
    }
}
