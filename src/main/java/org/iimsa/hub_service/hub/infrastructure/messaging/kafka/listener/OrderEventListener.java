package org.iimsa.hub_service.hub.infrastructure.messaging.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.iimsa.common.util.JsonUtil;
import org.iimsa.hub_service.hub.application.service.HubService;
import org.iimsa.hub_service.hub.domain.event.payload.OrderCreatedPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final HubService hubService;

    @IdempotentConsumer("order-created")
    @KafkaListener(
            topics = "${kafka.topics.order-created:order-fixed-requested}",
            groupId = "${spring.kafka.consumer.group-id:hub-group}"
    )
    public void handleOrderCreatedEvent(Message<String> message, Acknowledgment ack) {
        try {
            OrderCreatedPayload orderCreateEvent = JsonUtil.fromJson(message.getPayload(), OrderCreatedPayload.class);

            if (orderCreateEvent == null) {
                throw new IllegalArgumentException("Invalid order created event payload");
            }

            log.info("주문 이벤트 처리 시작: CorrelationId={}", orderCreateEvent.correlationId());

            hubService.processOrderCreatedEvent(orderCreateEvent);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("주문 생성 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // DLT (Dead Letter Topic)
    @KafkaListener(
            topics = "${kafka.topics.order-created:order-fixed-requested}.DLT",
            groupId = "${spring.kafka.consumer.group-id:hub-group}"
    )
    public void handleDLT(Message<String> message, Acknowledgment ack) {
        log.error("[DLT] 주문 생성 이벤트 최종 실패 (배송 라우팅 처리 불가)");
        try {
            OrderCreatedPayload orderCreateEvent = JsonUtil.fromJson(message.getPayload(), OrderCreatedPayload.class);
            log.error("실패한 OrderId={}, CorrelationId={}", orderCreateEvent.orderId(), orderCreateEvent.correlationId());
        } catch (Exception e) {
            log.error("DLT 메시지 파싱 실패: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }
}
