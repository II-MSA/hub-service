package org.iimsa.hub_service.hubroute.domain.cache;

import org.iimsa.hub_service.hubroute.domain.model.RouteTimeSource;

import java.time.LocalDateTime;

/**
 * Redis 배차 스냅샷 캐시 값
 * 키: hub:route:snapshot:{snapshotId}:{fromHubId}:{toHubId}
 * 스냅샷 ID: yyyyMMdd (자정 배차 기준 날짜)
 */
public record RouteSnapshotCache(

        /** 스냅샷 ID (yyyyMMdd) */
        String snapshotId,

        /** 예상 소요 시간 (분) */
        Integer duration,

        /** 예상 거리 (km) */
        Double distance,

        /** 값의 출처 */
        RouteTimeSource source,

        /** 스냅샷 생성 시각 */
        LocalDateTime createdAt
) {
    public static RouteSnapshotCache of(String snapshotId, Integer duration, Double distance, RouteTimeSource source) {
        return new RouteSnapshotCache(snapshotId, duration, distance, source, LocalDateTime.now());
    }
}
