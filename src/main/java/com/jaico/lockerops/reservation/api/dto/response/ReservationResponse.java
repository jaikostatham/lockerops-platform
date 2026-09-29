package com.jaico.lockerops.reservation.api.dto.response;

import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public class ReservationResponse {

    private Long id;
    private UUID reservationReference;
    private Long lockerCompartmentId;
    private Long lockerStationId;
    private Integer compartmentNumber;
    private ReservationStatus status;
    private Instant reservedFrom;
    private Instant reservedUntil;
    private String customerReference;
    private Long amountMinor;
    private String currency;
    private Instant paymentExpiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
    private Instant expiredAt;
    private Instant completedAt;

    public ReservationResponse(
            Long id,
            UUID reservationReference,
            Long lockerCompartmentId,
            Long lockerStationId,
            Integer compartmentNumber,
            ReservationStatus status,
            Instant reservedFrom,
            Instant reservedUntil,
            String customerReference,
            Long amountMinor,
            String currency,
            Instant paymentExpiresAt,
            Instant createdAt,
            Instant updatedAt,
            Instant cancelledAt,
            Instant expiredAt,
            Instant completedAt
    ) {
        this.id = id;
        this.reservationReference = reservationReference;
        this.lockerCompartmentId = lockerCompartmentId;
        this.lockerStationId = lockerStationId;
        this.compartmentNumber = compartmentNumber;
        this.status = status;
        this.reservedFrom = reservedFrom;
        this.reservedUntil = reservedUntil;
        this.customerReference = customerReference;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.paymentExpiresAt = paymentExpiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledAt = cancelledAt;
        this.expiredAt = expiredAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getReservationReference() {
        return reservationReference;
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

    public ReservationStatus getStatus() {
        return status;
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

    public Long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
