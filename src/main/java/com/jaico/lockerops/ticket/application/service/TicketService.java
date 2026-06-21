package com.jaico.lockerops.ticket.application.service;

import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.shared.exception.ApiErrorCode;
import com.jaico.lockerops.shared.exception.ApiException;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import com.jaico.lockerops.ticket.api.dto.response.TicketResponse;
import com.jaico.lockerops.ticket.application.mapper.TicketMapper;
import com.jaico.lockerops.ticket.domain.enums.TicketStatus;
import com.jaico.lockerops.ticket.domain.model.Ticket;
import com.jaico.lockerops.ticket.infrastructure.persistence.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AccessCodeService accessCodeService;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final TicketMapper ticketMapper;

    public TicketService(
            TicketRepository ticketRepository,
            AccessCodeService accessCodeService,
            TicketCodeGenerator ticketCodeGenerator,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.accessCodeService = accessCodeService;
        this.ticketCodeGenerator = ticketCodeGenerator;
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
        IssuedAccessCode issuedAccessCode = accessCodeService.issueAccessCode(
                savedTicket,
                reservation.getReservedFrom(),
                reservation.getReservedUntil()
        );

        return ticketMapper.toReservationTicketResponse(
                reservation,
                savedTicket,
                issuedAccessCode.getRawAccessCode()
        );
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
            ticketCode = ticketCodeGenerator.generate();
        } while (ticketRepository.existsByTicketCode(ticketCode));

        return ticketCode;
    }
}
