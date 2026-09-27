package com.jaico.lockerops.ticket.api.dto.response;

import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;

import java.time.Instant;

public class AccessCodeResponse {

    private Long id;
    private String codePreview;
    private AccessCodeStatus status;
    private Instant validFrom;
    private Instant expiresAt;
    private Integer useCount;

    public AccessCodeResponse(
            Long id,
            String codePreview,
            AccessCodeStatus status,
            Instant validFrom,
            Instant expiresAt,
            Integer useCount
    ) {
        this.id = id;
        this.codePreview = codePreview;
        this.status = status;
        this.validFrom = validFrom;
        this.expiresAt = expiresAt;
        this.useCount = useCount;
    }

    public Long getId() {
        return id;
    }

    public String getCodePreview() {
        return codePreview;
    }

    public AccessCodeStatus getStatus() {
        return status;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Integer getUseCount() {
        return useCount;
    }
}
