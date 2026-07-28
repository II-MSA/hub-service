package org.iimsa.hub_service.hubroute.domain.cache;

import org.iimsa.hub_service.hubroute.domain.model.RouteTimeSource;

import java.time.LocalDateTime;

/**
 * Redis live 캐시 값
 * 키: hub:route:live:{fromHubId}:{toHubId}
 */
public record LiveRouteCache(

        /** 예상 소요 시간 (분) */
        Integer duration,

        /** 예상 거리 (km) */
        Double distance,

        /** 값 갱신 시각 */
        LocalDateTime updatedAt,

        /** TTL 만료 기준 시각 (updatedAt + ttlMinutes) */
        LocalDateTime expiresAt,

        /** 값의 출처 */
        RouteTimeSource source
) {
    private static final int TTL_MINUTES = 15; // live 값 유효 시간

    public static LiveRouteCache of(Integer duration, Double distance, RouteTimeSource source) {
        LocalDateTime now = LocalDateTime.now();
        return new LiveRouteCache(duration, distance, now, now.plusMinutes(TTL_MINUTES), source);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
