package com.jaico.lockerops.ticket.domain.model;

import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;
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

@Entity
@Table(name = "access_codes")
public class AccessCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccessCodeStatus status;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "first_used_at")
    private Instant firstUsedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "use_count", nullable = false)
    private Integer useCount;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AccessCode() {
    }

    public AccessCode(
            String code,
            Ticket ticket,
            AccessCodeStatus status,
            Instant validFrom,
            Instant expiresAt
    ) {
        this.code = code;
        this.ticket = ticket;
        this.status = status;
        this.validFrom = validFrom;
        this.expiresAt = expiresAt;
        this.useCount = 0;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();

        if (useCount == null) {
            useCount = 0;
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

    public String getCode() {
        return code;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public AccessCodeStatus getStatus() {
        return status;
    }

    public void setStatus(AccessCodeStatus status) {
        this.status = status;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getFirstUsedAt() {
        return firstUsedAt;
    }

    public void setFirstUsedAt(Instant firstUsedAt) {
        this.firstUsedAt = firstUsedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
