package org.iimsa.hub_service.hub.infrastructure.client;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompanyClientFallbackFactory implements FallbackFactory<CompanyClient> {
    @Override
    public CompanyClient create(Throwable e) {
        return companyId -> {
            log.error("Company Service Fallback] ID: {} 조회 중 장애 발생. 사유: {}", companyId, e.getMessage(), e);
            throw new InternalServerErrorException("Company Service API 요청 처리 실패, 잠시 후 다시 시도해주세요.");
        };
    }
}
