package org.iimsa.hub_service.hubroute.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.model.RouteTimeSource;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * 허브 경로 소요시간 조회 — fallback 체인 처리
 *
 * <p>우선순위:
 * <ol>
 *   <li>REALTIME        — 허브 위도/경도로 외부 API 실시간 조회</li>
 *   <li>DB_AVERAGE      — p_hub_route_history 최근 30건 평균</li>
 *   <li>BASE_DURATION   — HubRoute 엔티티의 기본 저장값</li>
 *   <li>PREVIOUS_SNAPSHOT — 직전 배차 스냅샷</li>
 * </ol>
 *
 * <p>REALTIME 조회 시 {@link HubCoordinatePort}를 통해 Hub의 위도/경도를 조회합니다.
 * 좌표가 없는 허브는 실시간 조회를 건너뛰고 다음 fallback으로 진행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteTimeResolver {

    private final ExternalRouteTimeClient externalRouteTimeClient;
    private final HubCoordinatePort hubCoordinatePort;
    private final HubRouteHistoryRepository historyRepository;
    private final HubRouteCacheRepository cacheRepository;

    /**
     * fallback 체인을 순서대로 시도하여 소요시간 반환
     */
    public LiveRouteCache resolve(HubRoute hubRoute) {
        UUID from = hubRoute.getFromHubId();
        UUID to   = hubRoute.getToHubId();

        // 1. REALTIME — Hub 위도/경도 조회 후 외부 API 호출
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

        // 모든 fallback 실패
        log.warn("[FALLBACK 전체 실패] from={} to={} — null 반환", from, to);
        return LiveRouteCache.of(null, null, RouteTimeSource.BASE_DURATION);
    }

    /**
     * Hub 위도/경도를 조회하여 외부 API 실시간 소요시간 요청
     *
     * <p>출발 또는 도착 허브의 좌표가 없으면 REALTIME을 건너뜁니다.
     */
    private Optional<LiveRouteCache> fetchRealtime(UUID fromHubId, UUID toHubId) {
        var fromCoord = hubCoordinatePort.findCoordinate(fromHubId);
        if (fromCoord.isEmpty()) {
            log.debug("[REALTIME 스킵] 출발 허브 좌표 없음 fromHubId={}", fromHubId);
            return Optional.empty();
        }
        var toCoord = hubCoordinatePort.findCoordinate(toHubId);
        if (toCoord.isEmpty()) {
            log.debug("[REALTIME 스킵] 도착 허브 좌표 없음 toHubId={}", toHubId);
            return Optional.empty();
        }

        var result = externalRouteTimeClient.fetch(
                fromCoord.get().latitude(), fromCoord.get().longitude(),
                toCoord.get().latitude(),   toCoord.get().longitude()
        );

        if (result.isPresent()) {
            log.debug("[REALTIME] from={} to={}", fromHubId, toHubId);
            return Optional.of(LiveRouteCache.of(result.get().duration(), result.get().distance(), RouteTimeSource.REALTIME));
        }

        return Optional.empty();
    }
}
