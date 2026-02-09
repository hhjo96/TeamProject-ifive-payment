package com.spartaifive.commercepayment.domain.webhookevent.controller;

import com.spartaifive.commercepayment.domain.webhookevent.PortOneWebhookVerifier;
import com.spartaifive.commercepayment.domain.webhookevent.PortoneWebhookPayload;
import com.spartaifive.commercepayment.domain.webhookevent.service.WebhookEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PortOneWebhookVerifier verifier;
    private final ObjectMapper objectMapper;
    private final WebhookEventService webhookEventService;

    @PostMapping(value = "/portone-webhook", consumes = "application/json")
    public ResponseEntity<Void> handlePortoneWebhook(

            // 1. 검증용 원문
            @RequestBody byte[] rawBody,

            // 2. PortOne V2 필수 헤더
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {
        // (선택) 원문 로그
        log.info(
                "[PORTONE_WEBHOOK] id={} ts={} body={}",
                webhookId,
                webhookTimestamp,
                new String(rawBody, StandardCharsets.UTF_8)
        );

        // 3. 시그니처 검증 (rawBody 기준)
        boolean verified = verifier.verify(
                rawBody,
                webhookId,
                webhookTimestamp,
                webhookSignature
        );

        if (!verified) {
            log.warn("[PORTONE_WEBHOOK] signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4. 검증 통과 후 DTO 변환
        PortoneWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, PortoneWebhookPayload.class);
        } catch (Exception e) {
            log.error("[PORTONE_WEBHOOK] payload parse failed", e);
            return ResponseEntity.badRequest().build();
        }

        // 5. 이후부터는 “신뢰 가능한 데이터”
        log.info(
                "[PORTONE_WEBHOOK] VERIFIED paymentId={} status={}",
                payload.getPaymentId(),
                payload.getStatus()
        );
        webhookEventService.handleWebhookEvent(webhookId, payload);

        return ResponseEntity.ok().build();
    }
}
