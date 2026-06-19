package org.iimsa.hub_service.hubroute.infrastructure.persistence;

import org.iimsa.hub_service.hubroute.domain.model.HubRouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaHubRouteHistoryRepository extends JpaRepository<HubRouteHistory, UUID> {

    /**
     * 최근 30건 기준 평균 소요시간 (분)
     *
     * <p>PostgreSQL에서 집계 함수(AVG)와 ORDER BY를 같은 레벨에 쓸 수 없으므로,
     * 서브쿼리로 최근 30건을 먼저 선택한 뒤 평균을 계산합니다.
     */
    @Query(value = """
            SELECT AVG(actual_duration)
            FROM (
                SELECT actual_duration
                FROM p_hub_route_history
                WHERE from_hub_id = CAST(:fromHubId AS uuid)
                  AND to_hub_id   = CAST(:toHubId   AS uuid)
                ORDER BY recorded_at DESC
                LIMIT 30
            ) AS recent
            """, nativeQuery = true)
    Optional<Double> findAverageDuration(@Param("fromHubId") UUID fromHubId,
                                         @Param("toHubId") UUID toHubId);
}
