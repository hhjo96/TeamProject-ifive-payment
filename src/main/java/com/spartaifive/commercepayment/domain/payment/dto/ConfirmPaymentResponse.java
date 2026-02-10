package com.spartaifive.commercepayment.domain.payment.dto;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmPaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal actualAmount,
        String status,
        String portonePaymentId,
        LocalDateTime paid_at
) {
    public static ConfirmPaymentResponse from(Payment payment) {
        return new ConfirmPaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getActualAmount(),
                payment.getPaymentStatus().getStatusCode(),
                payment.getPortonePaymentId(),
                payment.getPaid_at()
        );
    }
}
