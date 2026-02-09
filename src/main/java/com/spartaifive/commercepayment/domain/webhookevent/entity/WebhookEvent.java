package com.spartaifive.commercepayment.domain.webhookevent.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WebhookEvent {

    //우리 db에서 관리할 키
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 포트원이랑 통신할때 사용할 키
    @Column(nullable = false, unique = true)
    private String webhookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;



    public WebhookEvent(String webhookId) {
        this.webhookId = webhookId;
        this.status = EventStatus.RECEIVED;
    }

    public void processed() {
        this.status = EventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void failed() {
        this.status = EventStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }



}
