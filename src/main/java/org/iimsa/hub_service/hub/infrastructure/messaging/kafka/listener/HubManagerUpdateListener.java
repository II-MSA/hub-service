package org.iimsa.hub_service.hub.infrastructure.messaging.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Manager;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.iimsa.common.util.JsonUtil;
import org.iimsa.hub_service.hub.application.service.HubService;
import org.iimsa.hub_service.hub.domain.model.HubManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubManagerUpdateListener {

    private final HubService hubService;

    @Transactional
    @IdempotentConsumer("hubManager-updated")
    @KafkaListener(topics = "${topics.companyManager.updated}", groupId = "hub-group")
    public void onUpdated(Message<String> message, Acknowledgment ack) {
        try {
            HubManager hubManager = JsonUtil.fromJson(message.getPayload(), HubManager.class);

            if (hubManager == null) {
                throw new IllegalArgumentException("Invalid hubManager update event payload");
            }

            // hubService.syncHubManagerInfo(hubManager); || 서비스 구현시 추가 필요
            
            log.info("허브 관리자 업데이트 처리 완료: hubManagerId={}", hubManager.getHubManagerId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("허브 관리자 업데이트 처리 실패:{}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${topics.companyManager.updated}.DLT", groupId = "company-group")
    public void handleDLT(Message<String> message, Acknowledgment ack) {
        log.error("DLT 수신 시작 (처리에 실패한 메시지가 DLT로 인입되었습니다)");

        try {
            HubManager hubManager = JsonUtil.fromJson(message.getPayload(), HubManager.class);
            log.error("허브 관리자 업데이트 최종 실패: hubManagerId={}", hubManager.getHubManagerId());
        } catch (Exception e) {
            log.error("DLT 메시지 변환 실패: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }

}
