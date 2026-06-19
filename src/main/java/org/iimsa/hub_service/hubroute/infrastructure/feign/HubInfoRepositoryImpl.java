package org.iimsa.hub_service.hubroute.infrastructure.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ticketing.common.exception.NotFoundException;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HubInfoRepository Feign 구현체
 *
 * <p>Hub 서비스 REST API를 Feign으로 호출하여 허브 정보를 제공합니다.
 */
@Slf4j
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
                data.hubId(),
                data.hubName(),
                data.address(),
                data.latitude(),
                data.longitude()
        );
    }

    /**
     * 허브 ID 집합에 대한 허브 정보를 일괄 조회합니다.
     *
     * <p>그래프 캐시 재빌드 시 A* 휴리스틱용 좌표를 한 번에 로드하기 위해 사용됩니다.
     * 캐시 미스(CRUD 이후 첫 경로 탐색)에서만 호출되므로 다소 비용이 높아도 허용됩니다.
     *
     * <p>개별 Feign 호출이 실패하면 해당 허브는 결과에서 제외합니다.
     * OptimalRouteCalculator 는 좌표가 없는 노드에 대해 h=0 으로 fallback 합니다.
     */
    @Override
    public Map<UUID, HubInfo> findHubsByIds(Set<UUID> hubIds) {
        if (hubIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, HubInfo> result = new HashMap<>(hubIds.size() * 2);
        for (UUID hubId : hubIds) {
            try {
                result.put(hubId, findHub(hubId));
            } catch (Exception e) {
                log.warn("허브 좌표 조회 실패 — hubId={}, 이유={}", hubId, e.getMessage());
            }
        }

        log.debug("허브 좌표 일괄 조회 완료 — 요청={}, 성공={}", hubIds.size(), result.size());
        return result;
    }
}
