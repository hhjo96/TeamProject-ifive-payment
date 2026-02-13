package com.spartaifive.commercepayment.domain.refund.entity;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refunds",
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_refunds_payment_id", columnNames = "payment_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private Payment payment;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refundReason", nullable = false, length = 100)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus refundStatus;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    public static Refund request(Payment payment, BigDecimal refundAmount, String refundReason) {
        if (payment == null) {
            throw new IllegalArgumentException("결제가 존재하지 않습니다");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("환불 금액은 0 이상이어야 합니다");
        }
        if (refundReason == null || refundReason.isBlank()) {
            throw new IllegalArgumentException("환불 사유는 필수 입니다");
        }

        Refund refund = new Refund();
        refund.payment = payment;
        refund.refundAmount = refundAmount;
        refund.refundReason = refundReason;
        refund.refundStatus = RefundStatus.REQUESTED;

        return refund;
    }

    public void complete() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundAt = LocalDateTime.now();
    }

    public void fail()

}
