package com.spartaifive.commercepayment.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
        @NotNull(message = "portonePayment ID는 필수입니다")
        String portonePaymentId,
        @NotNull(message = "주문 ID는 필수입니다")
        Long orderId
//        ,String clientRequestId
) {
}
