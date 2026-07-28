package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRouteEdgeKey;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteCacheRepository {

    // ── Live 캐시 ──────────────────────────────────

    Optional<LiveRouteCache> getLive(UUID fromHubId, UUID toHubId);

    void setLive(UUID fromHubId, UUID toHubId, LiveRouteCache cache);

    /**
     * 여러 구간의 live 캐시를 한 번에 조회합니다 (Redis MGET 파이프라인).
     *
     * <p>그래프 캐시 재빌드 시 구간 수만큼 개별 Redis 조회를 반복하는 대신,
     * 전체 구간을 한 번에 조회해 실시간 가중치를 그래프에 반영하기 위해 사용합니다.
     *
     * @param edges 조회할 (fromHubId, toHubId) 구간 키 집합
     * @return 캐시에 존재하는 항목만 담은 맵 (미스 항목은 결과에서 제외)
     */
    Map<HubRouteEdgeKey, LiveRouteCache> getLiveBulk(Collection<HubRouteEdgeKey> edges);
}
