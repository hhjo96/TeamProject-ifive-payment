package com.spartaifive.commercepayment.domain.payment.dto;

public record ConfirmPaymentRequest(
        String portonePaymentId,
        Long orderId,
        String merchantId
) {
}
