package org.iimsa.hub_service.hubroute.infrastructure.feign;

import lombok.RequiredArgsConstructor;
import org.iimsa.common.exception.NotFoundException;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * HubInfoRepository Feign 구현체
 *
 * <p>Hub 서비스 REST API를 Feign으로 호출하여 허브 정보를 제공합니다.
 */
@Repository
@RequiredArgsConstructor
public class HubInfoRepositoryImpl implements HubInfoRepository {

    private final HubFeignClient hubFeignClient;

    @Override
    public HubInfo findHub(UUID hubId) {
        var response = hubFeignClient.getHub(hubId);

        if (response == null || response.data() == null) {
            throw new NotFoundException("허브를 찾을 수 없습니다. hubId=" + hubId);
        }

        HubFeignResponse data = response.data();
        return new HubInfo(
                data.id(),
                data.name(),
                data.address(),
                data.latitude(),
                data.longitude()
        );
    }
}
