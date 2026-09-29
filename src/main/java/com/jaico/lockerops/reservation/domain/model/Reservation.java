package com.jaico.lockerops.reservation.domain.model;

import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_reference", nullable = false, unique = true, updatable = false)
    private UUID reservationReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_compartment_id", nullable = false)
    private LockerCompartment lockerCompartment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "reserved_from", nullable = false)
    private Instant reservedFrom;

    @Column(name = "reserved_until", nullable = false)
    private Instant reservedUntil;

    @Column(name = "customer_reference", length = 100)
    private String customerReference;

    @Column(name = "amount_minor", nullable = false)
    private Long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_expires_at")
    private Instant paymentExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Reservation() {
    }

    public Reservation(
            LockerCompartment lockerCompartment,
            ReservationStatus status,
            Instant reservedFrom,
            Instant reservedUntil,
            String customerReference
    ) {
        this(
                lockerCompartment,
                status,
                reservedFrom,
                reservedUntil,
                customerReference,
                0L,
                "EUR",
                null
        );
    }

    public Reservation(
            LockerCompartment lockerCompartment,
            ReservationStatus status,
            Instant reservedFrom,
            Instant reservedUntil,
            String customerReference,
            Long amountMinor,
            String currency,
            Instant paymentExpiresAt
    ) {
        this.lockerCompartment = lockerCompartment;
        this.status = status;
        this.reservedFrom = reservedFrom;
        this.reservedUntil = reservedUntil;
        this.customerReference = customerReference;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.paymentExpiresAt = paymentExpiresAt;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();

        if (reservationReference == null) {
            reservationReference = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getReservationReference() {
        return reservationReference;
    }

    public void setReservationReference(UUID reservationReference) {
        this.reservationReference = reservationReference;
    }

    public LockerCompartment getLockerCompartment() {
        return lockerCompartment;
    }

    public void setLockerCompartment(LockerCompartment lockerCompartment) {
        this.lockerCompartment = lockerCompartment;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Instant getReservedFrom() {
        return reservedFrom;
    }

    public void setReservedFrom(Instant reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(Instant reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
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

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
