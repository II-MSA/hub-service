package org.iimsa.hub_service.hub.infrastructure.provider;

import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.hub_service.hub.domain.service.UserProvider;
import org.iimsa.hub_service.hub.domain.service.dto.HubDeliveryManagerData;
import org.iimsa.hub_service.hub.infrastructure.client.UserClient;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubDeliveryManagerResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProviderImpl implements UserProvider {

    private final UserClient userClient;

    @Override
    public HubDeliveryManagerData getNextHubDeliveryManager(UUID hubId) {
        try {
            CommonResponse<HubDeliveryManagerResponse> res = userClient.getNextHubDeliveryManager(hubId);

            if (res == null || res.data() == null) {
                log.error("배정 가능한 배송 기사님이 없습니다. hubId: {}", hubId);
                throw new IllegalStateException("배송 기사 할당 실패: 가용한 기사님이 없습니다.");
            }

            HubDeliveryManagerResponse data = res.data();

            return new HubDeliveryManagerData(
                    data.deliverId(),
                    data.name(),
                    data.slackId(),
                    data.nextSequence()
            );

        } catch (FeignException.NotFound e) {
            log.warn("허브 배송 담당자를 찾을 수 없습니다. hubId={}", hubId);
            throw new IllegalStateException("배송 기사 할당 실패: 리소스를 찾을 수 없습니다.");
        } catch (FeignException e) {
            log.error("User 서비스 호출 실패(기사님 배정): hubId={}, Error={}", hubId, e.getMessage());
            throw new RuntimeException("배송 기사 정보 조회 중 외부 서비스 호출에 실패했습니다.", e);
        }
    }
}
