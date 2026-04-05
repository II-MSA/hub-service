package org.iimsa.hub_service.hub.infrastructure.client;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HubManagerClientFallbackFactory implements FallbackFactory<HubManagerClient> {
    @Override
    public HubManagerClient create(Throwable e) {
        return (hubManagerId) -> {
            log.error("User Service Fallback] STORE ID: {} 조회 중 장애 발생. 사유: {}", hubManagerId, e.getMessage(), e);
            throw new InternalServerErrorException("User Service API 요청 처리 실패, 잠시 후 다시 시도해주세요.");
        };
    }
}
