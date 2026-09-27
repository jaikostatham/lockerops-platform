package com.jaico.lockerops.ticket.infrastructure.persistence.repository;

import com.jaico.lockerops.ticket.domain.enums.AccessCodeStatus;
import com.jaico.lockerops.ticket.domain.model.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {

    Optional<AccessCode> findByTicket_IdAndCodeHash(Long ticketId, String codeHash);

    List<AccessCode> findByTicket_IdAndStatus(Long ticketId, AccessCodeStatus status);

    Optional<AccessCode> findFirstByTicket_IdOrderByCreatedAtDesc(Long ticketId);

    boolean existsByCodeHash(String codeHash);
}
