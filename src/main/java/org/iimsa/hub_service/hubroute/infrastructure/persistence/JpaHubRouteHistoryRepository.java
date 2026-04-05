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
     * 데이터가 쌓일수록 정확도 향상
     */
    @Query("""
            SELECT AVG(h.actualDuration)
            FROM HubRouteHistory h
            WHERE h.fromHubId = :fromHubId
              AND h.toHubId = :toHubId
            ORDER BY h.recordedAt DESC
            LIMIT 30
            """)
    Optional<Double> findAverageDuration(@Param("fromHubId") UUID fromHubId,
                                         @Param("toHubId") UUID toHubId);
}
