package com.jaico.lockerops.ticket.application.service;

import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import com.jaico.lockerops.ticket.api.dto.response.TicketResponse;
import com.jaico.lockerops.ticket.application.mapper.TicketMapper;
import com.jaico.lockerops.ticket.domain.enums.TicketStatus;
import com.jaico.lockerops.ticket.domain.model.AccessCode;
import com.jaico.lockerops.ticket.domain.model.Ticket;
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TicketService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String TICKET_CODE_PREFIX = "TCK-";
    private static final int TICKET_CODE_BOUND = 100_000_000;

    private final TicketRepository ticketRepository;
    private final AccessCodeService accessCodeService;
    private final TicketMapper ticketMapper;

    public TicketService(
            TicketRepository ticketRepository,
            AccessCodeService accessCodeService,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.accessCodeService = accessCodeService;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public ReservationTicketResponse issueTicketForReservation(Reservation reservation) {
        if (ticketRepository.existsByReservation_Id(reservation.getId())) {
            throw new ApiException(ApiErrorCode.TICKET_ALREADY_EXISTS_FOR_RESERVATION);
        }

        Instant issuedAt = Instant.now();

        Ticket ticket = new Ticket(
                generateUniqueTicketCode(),
                reservation,
                TicketStatus.ISSUED,
                issuedAt,
                reservation.getReservedUntil()
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        AccessCode accessCode = accessCodeService.issueAccessCode(
                savedTicket,
                reservation.getReservedFrom(),
                reservation.getReservedUntil()
        );

        return ticketMapper.toReservationTicketResponse(reservation, savedTicket, accessCode);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.TICKET_NOT_FOUND, id));

        return ticketMapper.toTicketResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ApiException(ApiErrorCode.TICKET_CODE_NOT_FOUND, ticketCode));

        return ticketMapper.toTicketResponse(ticket);
    }

    @Transactional
    public void cancelTicketForReservation(Reservation reservation, Instant cancelledAt) {
        ticketRepository.findByReservation_Id(reservation.getId())
                .ifPresent(ticket -> {
                    ticket.setStatus(TicketStatus.CANCELLED);
                    ticket.setCancelledAt(cancelledAt);
                    accessCodeService.revokeActiveAccessCodes(ticket, cancelledAt);
                });
    }

    private String generateUniqueTicketCode() {
        String ticketCode;

        do {
            ticketCode = TICKET_CODE_PREFIX + "%08d".formatted(SECURE_RANDOM.nextInt(TICKET_CODE_BOUND));
        } while (ticketRepository.existsByTicketCode(ticketCode));

        return ticketCode;
    }
}
