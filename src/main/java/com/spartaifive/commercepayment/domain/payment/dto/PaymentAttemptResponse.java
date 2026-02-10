package com.spartaifive.commercepayment.domain.payment.dto;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;

public record PaymentAttemptResponse(
        Long paymentId,
        BigDecimal expectedAmount,
        String status
) {
    public static PaymentAttemptResponse from(Payment payment) {
        return new PaymentAttemptResponse(
                payment.getId(),
                payment.getExpectedAmount(),
                payment.getPaymentStatus().getStatusCode()
        );
    }
}
