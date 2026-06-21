package com.jaico.lockerops.ticket.api.dto.response;

import com.jaico.lockerops.ticket.domain.enums.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public class TicketResponse {

    private Long id;
    private String ticketCode;
    private Long reservationId;
    private UUID reservationReference;
    private Long lockerCompartmentId;
    private Integer compartmentNumber;
    private TicketStatus status;
    private Instant issuedAt;
    private Instant expiresAt;

    public TicketResponse(
            Long id,
            String ticketCode,
            Long reservationId,
            UUID reservationReference,
            Long lockerCompartmentId,
            Integer compartmentNumber,
            TicketStatus status,
            Instant issuedAt,
            Instant expiresAt
    ) {
        this.id = id;
        this.ticketCode = ticketCode;
        this.reservationId = reservationId;
        this.reservationReference = reservationReference;
        this.lockerCompartmentId = lockerCompartmentId;
        this.compartmentNumber = compartmentNumber;
        this.status = status;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public UUID getReservationReference() {
        return reservationReference;
    }

    public Long getLockerCompartmentId() {
        return lockerCompartmentId;
    }

    public Integer getCompartmentNumber() {
        return compartmentNumber;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
