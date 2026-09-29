package com.jaico.lockerops.reservation.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;

public class CreateReservationRequest {

    @NotNull(message = "El identificador del compartimento es obligatorio")
    @Positive(message = "El identificador del compartimento debe ser mayor que cero")
    private Long lockerCompartmentId;

    @NotNull(message = "La duración de la reserva es obligatoria")
    @Positive(message = "La duración de la reserva debe ser mayor que cero")
    @Max(value = 1440, message = "La duración de la reserva no puede superar los 1440 minutos")
    private Integer durationMinutes;

    @Size(max = 100, message = "La referencia del cliente no puede superar los 100 caracteres")
    private String customerReference;

    public Long getLockerCompartmentId() {
        return lockerCompartmentId;
    }

    public void setLockerCompartmentId(Long lockerCompartmentId) {
        this.lockerCompartmentId = lockerCompartmentId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }
}
