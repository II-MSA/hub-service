package org.iimsa.hub_service.hub.infrastructure.client;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable e) {
        return hubId -> {
            log.error("[User Service Fallback] 배송 담당자 배정 중 장애 발생. Hub ID: {}, 사유: {}", hubId, e.getMessage(), e);
            throw new InternalServerErrorException("User Service API(배송 담당자 할당) 요청 처리 실패, 잠시 후 다시 시도해주세요.");
        };
    }
}
