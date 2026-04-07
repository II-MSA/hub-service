package org.iimsa.hub_service.hub.infrastructure.messaging.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.iimsa.common.util.JsonUtil;
import org.iimsa.hub_service.hub.application.service.HubService;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyUpdateListener {

    private final HubService hubService;

    @Transactional
    @IdempotentConsumer("company-updated")
    @KafkaListener(topics = "${topics.company.updated}", groupId = "hub-group")
    public void onUpdated(Message<String> message, Acknowledgment ack) {
        try {
            Company company = JsonUtil.fromJson(message.getPayload(), Company.class);

            if (company == null) {
                throw new IllegalArgumentException("Invalid company update event payload");
            }

            hubService.syncCompanyInfo(company);

            log.info("업체 업데이트 처리 완료: companyId={}", company.getId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("업체 업데이트 처리 실패:{}", e.getMessage(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${topics.company.updated}.DLT", groupId = "company-group")
    public void handleDLT(Message<String> message, Acknowledgment ack) {
        log.error("DLT 수신 시작 (처리에 실패한 메시지가 DLT로 인입되었습니다)");

        try {
            Company company = JsonUtil.fromJson(message.getPayload(), Company.class);
            log.error("업체 업데이트 최종 실패: companyId={}", company.getId());
        } catch (Exception e) {
            log.error("DLT 메시지 변환 실패: {}", e.getMessage());
        } finally {
            ack.acknowledge();
        }
    }

}
