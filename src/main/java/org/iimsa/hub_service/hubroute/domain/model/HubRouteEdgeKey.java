package org.iimsa.hub_service.hubroute.domain.model;

import java.util.UUID;

/**
 * 허브 경로 단일 구간(edge)을 식별하는 키.
 *
 * <p>Redis live 캐시 벌크 조회({@link org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository#getLiveBulk})와
 * 그래프 캐시 빌드 시 구간별 실시간 가중치를 매핑하는 데 사용됩니다.
 */
public record HubRouteEdgeKey(UUID fromHubId, UUID toHubId) {

    public static HubRouteEdgeKey of(UUID fromHubId, UUID toHubId) {
        return new HubRouteEdgeKey(fromHubId, toHubId);
    }
}
