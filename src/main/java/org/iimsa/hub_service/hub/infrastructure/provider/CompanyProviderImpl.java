package org.iimsa.hub_service.hub.infrastructure.provider;

import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hub.domain.service.CompanyProvider;
import org.iimsa.hub_service.hub.domain.service.dto.CompanyData;
import org.iimsa.hub_service.hub.infrastructure.client.CompanyClient;
import org.iimsa.hub_service.hub.infrastructure.client.dto.CompanyResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyProviderImpl implements CompanyProvider {

    private final CompanyClient client;

    @Override
    public CompanyData getCompany(UUID companyId) {

        try {
            CompanyResponse res = client.getCompany(companyId);
            return res == null || res.name() == null ? null : new CompanyData(res.id(), res.name());

        } catch (FeignException.NotFound e) {
            log.warn("Company를 찾을 수 없습니다. companyId={}", companyId);
            return null;

        } catch (FeignException e) {
            log.error("Company 서비스 호출 실패: companyId={}, Error={}", companyId, e.getMessage());
            throw new RuntimeException("업체 정보 조회 중 외부 서비스 호출에 실패했습니다.", e);
        }
    }{}
}
