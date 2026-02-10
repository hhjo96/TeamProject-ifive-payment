package com.spartaifive.commercepayment.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentAttemptRequest(
        @NotNull(message = "주문 ID는 필수 입니다.")
        Long orderId,
        String merchantId
) {
}
