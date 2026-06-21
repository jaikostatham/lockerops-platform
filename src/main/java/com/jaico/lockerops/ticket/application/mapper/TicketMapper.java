package com.jaico.lockerops.ticket.application.mapper;

import com.jaico.lockerops.compartment.domain.model.LockerCompartment;
import com.jaico.lockerops.reservation.domain.model.Reservation;
import com.jaico.lockerops.ticket.api.dto.response.AccessCodeResponse;
import com.jaico.lockerops.ticket.api.dto.response.AccessValidationResponse;
import com.jaico.lockerops.ticket.api.dto.response.ReservationTicketResponse;
import com.jaico.lockerops.ticket.api.dto.response.TicketResponse;
import com.jaico.lockerops.ticket.domain.model.AccessCode;
import com.jaico.lockerops.ticket.domain.model.Ticket;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TicketMapper {

    public TicketResponse toTicketResponse(Ticket ticket) {
        Reservation reservation = ticket.getReservation();
        LockerCompartment lockerCompartment = reservation.getLockerCompartment();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                reservation.getId(),
                reservation.getReservationReference(),
                lockerCompartment.getId(),
                lockerCompartment.getCompartmentNumber(),
                ticket.getStatus(),
                ticket.getIssuedAt(),
                ticket.getExpiresAt()
        );
    }

    public AccessCodeResponse toAccessCodeResponse(AccessCode accessCode) {
        return new AccessCodeResponse(
                accessCode.getId(),
                accessCode.getCode(),
                accessCode.getStatus(),
                accessCode.getValidFrom(),
                accessCode.getExpiresAt(),
                accessCode.getUseCount()
        );
    }

    public ReservationTicketResponse toReservationTicketResponse(
            Reservation reservation,
            Ticket ticket,
            AccessCode accessCode
    ) {
        LockerCompartment lockerCompartment = reservation.getLockerCompartment();

        return new ReservationTicketResponse(
                reservation.getId(),
                reservation.getReservationReference(),
                reservation.getStatus(),
                lockerCompartment.getId(),
                lockerCompartment.getLockerStation().getId(),
                lockerCompartment.getCompartmentNumber(),
                reservation.getReservedFrom(),
                reservation.getReservedUntil(),
                reservation.getCustomerReference(),
                ticket.getTicketCode(),
                accessCode.getCode()
        );
    }

    public AccessValidationResponse toAccessValidationResponse(
            AccessCode accessCode,
            Instant validatedAt
    ) {
        Ticket ticket = accessCode.getTicket();
        Reservation reservation = ticket.getReservation();
        LockerCompartment lockerCompartment = reservation.getLockerCompartment();

        return new AccessValidationResponse(
                true,
                ticket.getTicketCode(),
                reservation.getId(),
                lockerCompartment.getId(),
                lockerCompartment.getCompartmentNumber(),
                reservation.getReservedUntil(),
                validatedAt
        );
    }
}
