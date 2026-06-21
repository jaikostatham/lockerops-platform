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
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AccessCodeService {

    private final AccessCodeRepository accessCodeRepository;
    private final TicketRepository ticketRepository;
    private final AccessCodeGenerator accessCodeGenerator;
    private final AccessCodeHasher accessCodeHasher;
    private final TicketMapper ticketMapper;

    public AccessCodeService(
            AccessCodeRepository accessCodeRepository,
            TicketRepository ticketRepository,
            AccessCodeGenerator accessCodeGenerator,
            AccessCodeHasher accessCodeHasher,
            TicketMapper ticketMapper
    ) {
        this.accessCodeRepository = accessCodeRepository;
        this.ticketRepository = ticketRepository;
        this.accessCodeGenerator = accessCodeGenerator;
        this.accessCodeHasher = accessCodeHasher;
        this.ticketMapper = ticketMapper;
    }

    public IssuedAccessCode issueAccessCode(
            Ticket ticket,
            Instant validFrom,
            Instant expiresAt
    ) {
        String rawAccessCode = generateUniqueAccessCode();

        AccessCode accessCode = new AccessCode(
                accessCodeHasher.hash(rawAccessCode),
                accessCodeHasher.preview(rawAccessCode),
                ticket,
                AccessCodeStatus.ACTIVE,
                validFrom,
                expiresAt
        );

        AccessCode savedAccessCode = accessCodeRepository.save(accessCode);

        return new IssuedAccessCode(savedAccessCode, rawAccessCode);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AccessValidationResponse validateAccessCode(
            String ticketCode,
            String rawAccessCode
    ) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ApiException(ApiErrorCode.ACCESS_CREDENTIALS_INVALID));

        String codeHash = accessCodeHasher.hash(rawAccessCode);
        AccessCode accessCode = accessCodeRepository
                .findByTicket_IdAndCodeHash(ticket.getId(), codeHash)
                .orElseThrow(() -> new ApiException(ApiErrorCode.ACCESS_CREDENTIALS_INVALID));

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
        String rawAccessCode;
        String codeHash;

        do {
            rawAccessCode = accessCodeGenerator.generate();
            codeHash = accessCodeHasher.hash(rawAccessCode);
        } while (accessCodeRepository.existsByCodeHash(codeHash));

        return rawAccessCode;
    }
}
