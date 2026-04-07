package org.iimsa.hub_service.hub.infrastructure.provider;

import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hub.domain.service.HubManagerProvider;
import org.iimsa.hub_service.hub.domain.service.dto.CompanyData;
import org.iimsa.hub_service.hub.domain.service.dto.HubManagerData;
import org.iimsa.hub_service.hub.infrastructure.client.HubManagerClient;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubManagerResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubManagerProviderImple implements HubManagerProvider {

    private final HubManagerClient client;

    @Override
    public HubManagerData getHubManager(UUID hubManagerId) {
        try {
            HubManagerResponse res = client.getHubManager(hubManagerId);
            return res == null || res.name() == null ? null : new HubManagerData(res.id(), res.name());

        } catch (FeignException.NotFound e) {
            log.warn("허브 관리자를 찾을 수 없습니다. hubManagerId={}", hubManagerId);
            return null;

        } catch (FeignException e) {
            log.error("유저 서비스 호출 실패: hubManagerId={}, Error={}", hubManagerId, e.getMessage());
            throw new RuntimeException("허브 관리자 정보 조회 중 외부 서비스 호출에 실패했습니다.", e);
        }
    }
}
