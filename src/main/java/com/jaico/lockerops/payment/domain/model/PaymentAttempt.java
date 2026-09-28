package com.jaico.lockerops.payment.domain.model;

import com.jaico.lockerops.payment.domain.enums.PaymentStatus;
import com.jaico.lockerops.reservation.domain.model.Reservation;
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
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true, updatable = false)
    private UUID paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "amount_minor", nullable = false)
    private Long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PaymentAttempt() {
    }

    public PaymentAttempt(
            Reservation reservation,
            PaymentStatus status,
            Long amountMinor,
            String currency,
            Instant processedAt
    ) {
        this.reservation = reservation;
        this.status = status;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.processedAt = processedAt;
    }

    @PrePersist
    public void prePersist() {
        if (paymentReference == null) {
            paymentReference = UUID.randomUUID();
        }

        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getPaymentReference() {
        return paymentReference;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public PaymentStatus getStatus() {
        return status;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
