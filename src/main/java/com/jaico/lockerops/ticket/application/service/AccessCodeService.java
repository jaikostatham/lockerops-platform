package com.jaico.lockerops.ticket.application.service;

import com.jaico.lockerops.reservation.domain.enums.ReservationStatus;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.ticket.api.dto.response.AccessValidationResponse;
import com.jaico.lockerops.ticket.application.mapper.TicketMapper;
import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;
import com.jaico.lockerops.ticket.domain.enums.TicketStatus;
import com.jaico.lockerops.ticket.domain.model.AccessCode;
import com.jaico.lockerops.ticket.domain.model.Ticket;
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
public class AccessCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int ACCESS_CODE_BOUND = 1_000_000;

    private final AccessCodeRepository accessCodeRepository;
    private final TicketMapper ticketMapper;

    public AccessCodeService(
            AccessCodeRepository accessCodeRepository,
            TicketMapper ticketMapper
    ) {
        this.accessCodeRepository = accessCodeRepository;
        this.ticketMapper = ticketMapper;
    }

    public AccessCode issueAccessCode(
            Ticket ticket,
            Instant validFrom,
            Instant expiresAt
    ) {
        AccessCode accessCode = new AccessCode(
                generateUniqueAccessCode(),
                ticket,
                AccessCodeStatus.ACTIVE,
                validFrom,
                expiresAt
        );

        return accessCodeRepository.save(accessCode);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AccessValidationResponse validateAccessCode(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ApiErrorCode.ACCESS_CODE_NOT_FOUND));

        Instant now = Instant.now();

        validateAccessCodeCanBeUsed(accessCode, now);
        registerSuccessfulUse(accessCode, now);

        return ticketMapper.toAccessValidationResponse(accessCode, now);
    }

    public void revokeActiveAccessCodes(Ticket ticket, Instant revokedAt) {
        List<AccessCode> activeAccessCodes =
                accessCodeRepository.findByTicket_IdAndStatus(ticket.getId(), AccessCodeStatus.ACTIVE);

        activeAccessCodes.forEach(accessCode -> {
            accessCode.setStatus(AccessCodeStatus.REVOKED);
            accessCode.setRevokedAt(revokedAt);
        });
    }

    private void validateAccessCodeCanBeUsed(AccessCode accessCode, Instant now) {
        Ticket ticket = accessCode.getTicket();

        if (accessCode.getStatus() == AccessCodeStatus.REVOKED) {
            throw new ApiException(ApiErrorCode.ACCESS_CODE_REVOKED);
        }

        if (accessCode.getStatus() == AccessCodeStatus.EXPIRED) {
            throw new ApiException(ApiErrorCode.ACCESS_CODE_EXPIRED);
        }

        if (accessCode.getStatus() != AccessCodeStatus.ACTIVE) {
            throw new ApiException(ApiErrorCode.ACCESS_CODE_NOT_ACTIVE);
        }

        if (now.isBefore(accessCode.getValidFrom())) {
            throw new ApiException(ApiErrorCode.ACCESS_CODE_NOT_ACTIVE);
        }

        if (now.isAfter(accessCode.getExpiresAt())) {
            markAccessCodeAndTicketAsExpired(accessCode, ticket, now);
            throw new ApiException(ApiErrorCode.ACCESS_CODE_EXPIRED);
        }

        if (ticket.getStatus() != TicketStatus.ISSUED) {
            throw new ApiException(ApiErrorCode.TICKET_NOT_ACTIVE);
        }

        if (ticket.getReservation().getStatus() != ReservationStatus.CONFIRMED) {
            throw new ApiException(ApiErrorCode.ACCESS_CODE_RESERVATION_NOT_ACTIVE);
        }
    }

    private void registerSuccessfulUse(AccessCode accessCode, Instant usedAt) {
        if (accessCode.getFirstUsedAt() == null) {
            accessCode.setFirstUsedAt(usedAt);
        }

        accessCode.setLastUsedAt(usedAt);
        accessCode.setUseCount(accessCode.getUseCount() + 1);
    }

    private void markAccessCodeAndTicketAsExpired(
            AccessCode accessCode,
            Ticket ticket,
            Instant expiredAt
    ) {
        accessCode.setStatus(AccessCodeStatus.EXPIRED);

        if (ticket.getStatus() == TicketStatus.ISSUED) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticket.setExpiredAt(expiredAt);
        }
    }

    private String generateUniqueAccessCode() {
        String code;

        do {
            code = "%06d".formatted(SECURE_RANDOM.nextInt(ACCESS_CODE_BOUND));
        } while (accessCodeRepository.existsByCode(code));

        return code;
    }
}
