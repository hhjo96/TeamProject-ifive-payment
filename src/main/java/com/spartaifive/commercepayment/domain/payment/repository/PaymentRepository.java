package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPortonePaymentId(String portonePaymentId);
    List<Payment> findAllByOrder_id(Long orderId);
}
