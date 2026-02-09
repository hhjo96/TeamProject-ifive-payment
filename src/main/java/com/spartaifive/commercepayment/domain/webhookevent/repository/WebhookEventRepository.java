package com.spartaifive.commercepayment.domain.webhookevent.repository;

import com.spartaifive.commercepayment.domain.webhookevent.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent,Long> {
    boolean existsByWebhookId(String webhookId);
}
