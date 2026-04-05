package org.iimsa.hub_service.hubroute.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.cache.RouteSnapshotCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.application.service.RouteTimeResolver;
import org.springframework.stereotype.Service;

/**
 * 배차 시점 경로 소요시간 스냅샷 조회
 *
 * <p>23:55에 생성된 스냅샷을 우선 반환하고,
 * 스냅샷이 없는 경우 {@link RouteTimeResolver} fallback 체인으로 대체값을 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteSnapshotService {

    private final HubRouteCacheRepository cacheRepository;
    private final RouteTimeResolver routeTimeResolver;

    /**
     * 배차용 스냅샷 조회
     *
     * <p>우선순위:
     * <ol>
     *   <li>오늘 날짜 스냅샷 (hub:route:snapshot:{latestSnapshotId}:{from}:{to})</li>
     *   <li>스냅샷 미존재 → RouteTimeResolver fallback 체인 결과를 임시 스냅샷으로 래핑</li>
     * </ol>
     *
     * @param hubRoute 조회할 허브 경로 엔티티
     * @return 배차에 사용할 소요시간 스냅샷
     */
    public RouteSnapshotCache getSnapshotForDispatch(HubRoute hubRoute) {
        var from = hubRoute.getFromHubId();
        var to   = hubRoute.getToHubId();

        // 1. 최신 스냅샷 ID 조회
        var latestSnapshotId = cacheRepository.getLatestSnapshotId();
        if (latestSnapshotId.isPresent()) {
            var snapshot = cacheRepository.getSnapshot(latestSnapshotId.get(), from, to);
            if (snapshot.isPresent()) {
                log.debug("[SNAPSHOT] snapshotId={} from={} to={}", latestSnapshotId.get(), from, to);
                return snapshot.get();
            }
            log.warn("[SNAPSHOT 누락] snapshotId={} from={} to={} — fallback 진행", latestSnapshotId.get(), from, to);
        } else {
            log.warn("[SNAPSHOT ID 없음] from={} to={} — 아직 스냅샷 생성 전 (초기 배차)", from, to);
        }

        // 2. 스냅샷 없음 → RouteTimeResolver fallback 체인으로 대체
        LiveRouteCache live = routeTimeResolver.resolve(hubRoute);
        String fallbackSnapshotId = "FALLBACK";

        log.info("[SNAPSHOT FALLBACK] from={} to={} source={}", from, to, live.source());
        return RouteSnapshotCache.of(fallbackSnapshotId, live.duration(), live.distance(), live.source());
    }
}
