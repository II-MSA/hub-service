package org.iimsa.hub_service.hubroute.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.cache.RouteSnapshotCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.iimsa.hub_service.hubroute.application.service.RouteTimeResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 허브 경로 배차 스냅샷 생성 스케줄러
 *
 * <p>매일 23:55에 전체 활성 허브 경로의 소요시간을 스냅샷으로 저장합니다.
 * 다음날 00:00 배차 시 이 스냅샷을 기준값으로 사용합니다.
 *
 * <p>모든 경로 저장이 완료된 후에만 {@code hub:route:snapshot:latest} 키를 갱신합니다.
 * 스냅샷 생성 중 실패 시 이전 날 스냅샷이 자연스럽게 유지됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteSnapshotScheduler {

    private static final DateTimeFormatter SNAPSHOT_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final HubRouteRepository hubRouteRepository;
    private final HubRouteCacheRepository cacheRepository;
    private final RouteTimeResolver routeTimeResolver;

    /**
     * 매일 23:55 배차 스냅샷 생성
     *
     * <p>스냅샷 ID는 오늘 날짜 (yyyyMMdd).
     * 전체 경로 저장 완료 후에만 latestSnapshotId를 갱신하여 원자성을 보장합니다.
     * 일부 경로 실패 시에도 나머지 경로 스냅샷은 저장하고, latestSnapshotId는 갱신합니다.
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void createSnapshot() {
        String snapshotId = LocalDate.now().format(SNAPSHOT_ID_FORMATTER);
        List<HubRoute> routes = hubRouteRepository.findAllActive();

        if (routes.isEmpty()) {
            log.warn("[SNAPSHOT] 활성 경로 없음 — snapshotId={} 생성 스킵", snapshotId);
            return;
        }

        log.info("[SNAPSHOT] 시작 — snapshotId={} 총 {}개 경로", snapshotId, routes.size());
        int successCount = 0;
        int failCount    = 0;

        for (HubRoute route : routes) {
            try {
                LiveRouteCache live = routeTimeResolver.resolve(route);
                RouteSnapshotCache snapshot = RouteSnapshotCache.of(
                        snapshotId,
                        live.duration(),
                        live.distance(),
                        live.source()
                );
                cacheRepository.setSnapshot(snapshotId, route.getFromHubId(), route.getToHubId(), snapshot);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("[SNAPSHOT] 저장 실패 snapshotId={} from={} to={} error={}",
                        snapshotId, route.getFromHubId(), route.getToHubId(), e.getMessage());
            }
        }

        // 모든 경로 처리 후 latestSnapshotId 갱신
        // 실패가 있어도 성공한 경로가 하나라도 있으면 갱신
        if (successCount > 0) {
            cacheRepository.setLatestSnapshotId(snapshotId);
            log.info("[SNAPSHOT] 완료 — snapshotId={} 성공={} 실패={}", snapshotId, successCount, failCount);
        } else {
            log.error("[SNAPSHOT] 전체 실패 — snapshotId={} latestSnapshotId 갱신 안 함 (이전 스냅샷 유지)",
                    snapshotId);
        }
    }
}
