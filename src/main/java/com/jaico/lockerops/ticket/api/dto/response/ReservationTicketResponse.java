package com.jaico.lockerops.ticket.api.dto.response;

import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public class ReservationTicketResponse {

    private Long reservationId;
    private UUID reservationReference;
    private ReservationStatus reservationStatus;
    private Long lockerCompartmentId;
    private Long lockerStationId;
    private Integer compartmentNumber;
    private Instant reservedFrom;
    private Instant reservedUntil;
    private String customerReference;
    private String ticketCode;
    private String accessCode;

    public ReservationTicketResponse(
            Long reservationId,
            UUID reservationReference,
            ReservationStatus reservationStatus,
            Long lockerCompartmentId,
            Long lockerStationId,
            Integer compartmentNumber,
            Instant reservedFrom,
            Instant reservedUntil,
            String customerReference,
            String ticketCode,
            String accessCode
    ) {
        this.reservationId = reservationId;
        this.reservationReference = reservationReference;
        this.reservationStatus = reservationStatus;
        this.lockerCompartmentId = lockerCompartmentId;
        this.lockerStationId = lockerStationId;
        this.compartmentNumber = compartmentNumber;
        this.reservedFrom = reservedFrom;
        this.reservedUntil = reservedUntil;
        this.customerReference = customerReference;
        this.ticketCode = ticketCode;
        this.accessCode = accessCode;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public UUID getReservationReference() {
        return reservationReference;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public Long getLockerCompartmentId() {
        return lockerCompartmentId;
    }

    public Long getLockerStationId() {
        return lockerStationId;
    }

    public Integer getCompartmentNumber() {
        return compartmentNumber;
    }

    public Instant getReservedFrom() {
        return reservedFrom;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getAccessCode() {
        return accessCode;
    }
}
