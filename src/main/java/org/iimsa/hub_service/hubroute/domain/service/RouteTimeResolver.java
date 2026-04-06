package org.iimsa.hub_service.hubroute.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.model.RouteTimeSource;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * 허브 경로 소요시간 조회 — fallback 체인 처리 도메인 서비스
 *
 * <p>우선순위:
 * <ol>
 *   <li>REALTIME        — 허브 좌표로 외부 API 실시간 조회</li>
 *   <li>DB_AVERAGE      — p_hub_route_history 최근 30건 평균</li>
 *   <li>BASE_DURATION   — HubRoute 엔티티의 기본 저장값</li>
 *   <li>PREVIOUS_SNAPSHOT — 직전 배차 스냅샷</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteTimeResolver {

    private final ExternalRouteTimeClient externalRouteTimeClient;
    private final HubInfoRepository hubInfoRepository;
    private final HubRouteHistoryRepository historyRepository;
    private final HubRouteCacheRepository cacheRepository;

    public LiveRouteCache resolve(HubRoute hubRoute) {
        UUID from = hubRoute.getFromHubId();
        UUID to   = hubRoute.getToHubId();

        // 1. REALTIME — Hub 좌표 조회 후 외부 API 호출
        Optional<LiveRouteCache> realtimeResult = fetchRealtime(from, to);
        if (realtimeResult.isPresent()) {
            return realtimeResult.get();
        }

        // 2. DB_AVERAGE
        var avgDuration = historyRepository.findAverageDuration(from, to);
        if (avgDuration.isPresent()) {
            log.debug("[DB_AVERAGE] from={} to={} avg={}분", from, to, avgDuration.get());
            return LiveRouteCache.of(avgDuration.get().intValue(), hubRoute.getEstimatedDistance(), RouteTimeSource.DB_AVERAGE);
        }

        // 3. BASE_DURATION
        if (hubRoute.getEstimatedDuration() != null) {
            log.debug("[BASE_DURATION] from={} to={}", from, to);
            return LiveRouteCache.of(hubRoute.getEstimatedDuration(), hubRoute.getEstimatedDistance(), RouteTimeSource.BASE_DURATION);
        }

        // 4. PREVIOUS_SNAPSHOT
        var latestSnapshotId = cacheRepository.getLatestSnapshotId();
        if (latestSnapshotId.isPresent()) {
            var snapshot = cacheRepository.getSnapshot(latestSnapshotId.get(), from, to);
            if (snapshot.isPresent()) {
                log.debug("[PREVIOUS_SNAPSHOT] from={} to={} snapshotId={}", from, to, latestSnapshotId.get());
                return LiveRouteCache.of(snapshot.get().duration(), snapshot.get().distance(), RouteTimeSource.PREVIOUS_SNAPSHOT);
            }
        }

        log.warn("[FALLBACK 전체 실패] from={} to={} — null 반환", from, to);
        return LiveRouteCache.of(null, null, RouteTimeSource.BASE_DURATION);
    }

    private Optional<LiveRouteCache> fetchRealtime(UUID fromHubId, UUID toHubId) {
        HubInfo fromHub = hubInfoRepository.findHub(fromHubId);
        HubInfo toHub   = hubInfoRepository.findHub(toHubId);

        if (!fromHub.hasCoordinate()) {
            log.debug("[REALTIME 스킵] 출발 허브 좌표 없음 fromHubId={}", fromHubId);
            return Optional.empty();
        }
        if (!toHub.hasCoordinate()) {
            log.debug("[REALTIME 스킵] 도착 허브 좌표 없음 toHubId={}", toHubId);
            return Optional.empty();
        }

        var result = externalRouteTimeClient.fetch(
                fromHub.latitude(), fromHub.longitude(),
                toHub.latitude(),   toHub.longitude()
        );

        if (result.isPresent()) {
            log.debug("[REALTIME] from={} to={}", fromHubId, toHubId);
            return Optional.of(LiveRouteCache.of(result.get().duration(), result.get().distance(), RouteTimeSource.REALTIME));
        }

        return Optional.empty();
    }
}
