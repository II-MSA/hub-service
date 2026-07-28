package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.model.HubRouteHistory;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteHistoryRepository {

    HubRouteHistory save(HubRouteHistory history);

    /** 최근 N건 기준 평균 소요시간 (분) — DB_AVERAGE fallback 용 */
    Optional<Double> findAverageDuration(UUID fromHubId, UUID toHubId);
}
