package org.iimsa.hub_service.hub.infrastructure.client;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HubRouteClientFallbackFactory implements FallbackFactory<HubRouteClient> {
    @Override
    public HubRouteClient create(Throwable e) {
        return (originHubId, destinationHubId) -> {
            log.error("[Hub Route Service Fallback] 경로 조회 중 장애 발생. StartHub: {}, EndHub: {}, 사유: {}", originHubId, destinationHubId, e.getMessage(), e);
            throw new InternalServerErrorException("경로 조회 API 요청 처리 실패, 잠시 후 다시 시도해주세요.");
        };
    }
}
