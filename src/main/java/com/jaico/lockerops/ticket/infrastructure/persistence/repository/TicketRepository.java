package com.jaico.lockerops.ticket.infrastructure.persistence.repository;

import com.jaico.lockerops.ticket.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    Optional<Ticket> findByReservation_Id(Long reservationId);

    boolean existsByTicketCode(String ticketCode);

    boolean existsByReservation_Id(Long reservationId);
}
