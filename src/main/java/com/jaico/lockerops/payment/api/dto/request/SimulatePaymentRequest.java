package com.jaico.lockerops.payment.api.dto.request;

import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SimulatePaymentRequest {

    @NotNull(message = "La referencia de la reserva es obligatoria")
    private UUID reservationReference;

    @NotNull(message = "El resultado simulado del pago es obligatorio")
    private PaymentStatus outcome;

    public UUID getReservationReference() {
        return reservationReference;
    }

    public void setReservationReference(UUID reservationReference) {
        this.reservationReference = reservationReference;
    }

    public PaymentStatus getOutcome() {
        return outcome;
    }

    public void setOutcome(PaymentStatus outcome) {
        this.outcome = outcome;
    }
}
