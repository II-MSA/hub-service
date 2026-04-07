package org.iimsa.hub_service.hubroute.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.iimsa.hub_service.hubroute.domain.service.RouteTimeResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 허브 경로 live 소요시간 주기적 갱신 스케줄러
 *
 * <p>10분마다 전체 활성 허브 경로를 순회하여 Redis live 캐시를 갱신합니다.
 * {@link RouteTimeResolver} fallback 체인을 통해
 * 실시간 → DB 평균 → 기본값 → 직전 스냅샷 순으로 최선의 값을 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteTimeRefreshScheduler {

    private final HubRouteRepository hubRouteRepository;
    private final HubRouteCacheRepository cacheRepository;
    private final RouteTimeResolver routeTimeResolver;

    /**
     * 10분마다 전체 경로 live 캐시 갱신
     *
     * <p>개별 경로 갱신 실패는 로그만 남기고 계속 진행합니다.
     * (한 경로 실패가 전체 갱신을 중단시키지 않도록)
     */
    @Scheduled(fixedRate = 600_000) // 10분
    public void refresh() {
        List<HubRoute> routes = hubRouteRepository.findAllActive();
        if (routes.isEmpty()) {
            log.debug("[LIVE_REFRESH] 활성 경로 없음 — 스킵");
            return;
        }

        log.info("[LIVE_REFRESH] 시작 — 총 {}개 경로", routes.size());
        int successCount = 0;
        int failCount    = 0;

        for (HubRoute route : routes) {
            try {
                LiveRouteCache live = routeTimeResolver.resolve(route);
                cacheRepository.setLive(route.getFromHubId(), route.getToHubId(), live);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("[LIVE_REFRESH] 갱신 실패 from={} to={} error={}",
                        route.getFromHubId(), route.getToHubId(), e.getMessage());
            }
        }

        log.info("[LIVE_REFRESH] 완료 — 성공={} 실패={}", successCount, failCount);
    }
}
