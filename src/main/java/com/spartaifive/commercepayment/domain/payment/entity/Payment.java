package com.spartaifive.commercepayment.domain.payment.entity;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_portone_payment_id",
                        columnNames = "portone_payment_id")
        }
)
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // payment Record ID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_order"))
    private Order order;
    /**
     * PortOne 결제 결과 ID
     * - Attempt 단계에서는 null
     * - Confirm/Webhook에서 세팅
     * - UNIQUE로 멱등성 보장
     */
    @Column(name = "portone_payment_id", length = 100)
    private String portonePaymentId;
    /**
     * 결제 요청 식별자
     * - 주문 / 결제 조회 ID
     */
    @Column(name = "merchant_payment_id", length = 100)
    private String merchantPaymentId;

    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;
    /**
     * 서버가 예상하는 결제 금액 (스냅샷)
     */
    @Column(name = "expected_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal expectedAmount;
    /**
     * PortOne에서 조회한 결제 승인 금액
     */
    @Column(name = "actual_amount", precision = 15, scale = 0)
    private BigDecimal actualAmount;

    @Column(name = "paid_at")
    private LocalDateTime paid_at;

    @Column(name = "refunded_at")
    private LocalDateTime refunded_at;
}
