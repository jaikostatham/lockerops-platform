package com.jaico.lockerops.payment.infrastructure.persistence.repository;

import com.jaico.lockerops.payment.domain.model.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
}
