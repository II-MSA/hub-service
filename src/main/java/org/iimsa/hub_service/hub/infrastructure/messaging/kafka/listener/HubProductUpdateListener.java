package org.iimsa.hub_service.hub.infrastructure.messaging.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.iimsa.common.util.JsonUtil;
import org.iimsa.hub_service.hub.application.service.HubService;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.iimsa.hub_service.hub.domain.model.HubProduct;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubProductUpdateListener {

    private final HubService hubService;

    @Transactional
    @IdempotentConsumer("hubProduct-updated")
    @KafkaListener(topics = "${topics.hubProduct.updated}", groupId = "hub-group")
    public void onUpdated(Message<String> message, Acknowledgment ack) {
        try {
            HubProduct hubProduct = JsonUtil.fromJson(message.getPayload(), HubProduct.class);

            if (hubProduct == null) {
                throw new IllegalArgumentException("Invalid hub-product update event payload");
            }

            // hubService.syncHubProductInfo(hubProduct); || 허브 서비스 구현시 추가 필요

            log.info("허브 상품 업데이트 처리 완료: hubProductId={}", hubProduct.getId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("허브 상품 업데이트 처리 실패:{}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${topics.hubProduct.updated}.DLT", groupId = "hub-group")
    public void handleDLT(Message<String> message, Acknowledgment ack) {
        log.error("DLT 수신 시작 (처리에 실패한 메시지가 DLT로 인입되었습니다)");

        try {
            HubProduct hubProduct = JsonUtil.fromJson(message.getPayload(), HubProduct.class);
            log.error("허브 상품 업데이트 최종 실패: hubProductId={}", hubProduct.getId());
        } catch (Exception e) {
            log.error("DLT 메시지 변환 실패: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }

}
