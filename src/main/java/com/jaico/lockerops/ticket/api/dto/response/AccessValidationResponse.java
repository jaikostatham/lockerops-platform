package com.jaico.lockerops.ticket.api.dto.response;

import java.time.Instant;

public class AccessValidationResponse {

    private boolean granted;
    private String ticketCode;
    private Long reservationId;
    private Long lockerCompartmentId;
    private Integer compartmentNumber;
    private Instant reservedUntil;
    private Instant validatedAt;

    public AccessValidationResponse(
            boolean granted,
            String ticketCode,
            Long reservationId,
            Long lockerCompartmentId,
            Integer compartmentNumber,
            Instant reservedUntil,
            Instant validatedAt
    ) {
        this.granted = granted;
        this.ticketCode = ticketCode;
        this.reservationId = reservationId;
        this.lockerCompartmentId = lockerCompartmentId;
        this.compartmentNumber = compartmentNumber;
        this.reservedUntil = reservedUntil;
        this.validatedAt = validatedAt;
    }

    public boolean isGranted() {
        return granted;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getLockerCompartmentId() {
        return lockerCompartmentId;
    }

    public Integer getCompartmentNumber() {
        return compartmentNumber;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }
}
