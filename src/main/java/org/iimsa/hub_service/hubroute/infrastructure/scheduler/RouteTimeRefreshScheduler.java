package org.iimsa.hub_service.hubroute.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.iimsa.hub_service.hubroute.domain.service.OptimalRouteCalculator;
import org.iimsa.hub_service.hubroute.domain.service.RouteTimeResolver;
import org.iimsa.hub_service.hubroute.infrastructure.lock.SchedulerDistributedLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 허브 경로 live 소요시간 주기적 갱신 스케줄러
 *
 * <p>10분마다 전체 활성 허브 경로를 순회하여 Redis live 캐시를 갱신합니다.
 * {@link RouteTimeResolver} fallback 체인을 통해
 * 실시간 → DB 평균 → 기본값 순으로 최선의 값을 사용합니다.
 *
 * <p>갱신이 끝나면 {@link OptimalRouteCalculator#invalidateGraph()}를 호출해
 * A* / Dijkstra 그래프 캐시를 무효화합니다. 다음 경로 탐색 요청이 그래프를 재빌드할 때
 * {@link HubRouteCacheRepository#getLiveBulk}로 방금 갱신된 실시간 소요시간을
 * 벌크 조회해 구간 가중치에 반영합니다 — 탐색 중 개별 Redis 조회 없이, 주기적
 * 벌크 갱신만으로 실시간성을 반영하는 구조입니다.
 *
 * <p><b>다중 인스턴스 중복 실행 방지</b>: hub-service가 여러 인스턴스로 떠 있으면
 * {@code @Scheduled} 는 인스턴스마다 독립적으로 실행되므로, {@link SchedulerDistributedLock}
 * 으로 한 번에 하나의 인스턴스만 실제 갱신을 수행하도록 제한합니다. Redis 장애로 락
 * 자체를 못 잡는 경우에는 "코디네이션 실패"로 보고 락 없이 그냥 실행합니다(fail-open) —
 * 갱신 작업 자체가 멱등적(각 구간 최신값으로 덮어쓰기)이라 중복 실행되어도 데이터가
 * 깨지지 않는 반면, 반대로 "락을 못 잡으면 스킵"으로 처리하면 Redis 장애 중엔 모든
 * 인스턴스가 갱신을 건너뛰어 데이터가 계속 stale 해지기 때문입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteTimeRefreshScheduler {

    private final HubRouteRepository hubRouteRepository;
    private final HubRouteCacheRepository cacheRepository;
    private final RouteTimeResolver routeTimeResolver;
    private final OptimalRouteCalculator optimalRouteCalculator;
    private final SchedulerDistributedLock distributedLock;

    private static final String LOCK_TASK_NAME = "route-time-refresh";

    /** 락 유지 시간 — 스케줄 주기(10분)보다 짧게 잡아, unlock 이 호출되지 못해도 다음 스케줄 전에 자동 해제되도록 함 */
    private static final Duration LOCK_TTL = Duration.ofMinutes(9);

    /**
     * 10분마다 전체 경로 live 캐시 갱신 (분산 락으로 인스턴스 중 하나만 실제 수행)
     */
    @Scheduled(fixedRate = 600_000) // 10분
    public void refresh() {
        Optional<String> lockToken;
        try {
            lockToken = distributedLock.tryLock(LOCK_TASK_NAME, LOCK_TTL);
        } catch (Exception e) {
            log.warn("[LIVE_REFRESH] 분산 락 획득 시도 중 오류(Redis 장애 추정) — 락 없이 진행. error={}", e.getMessage());
            doRefresh();
            return;
        }

        if (lockToken.isEmpty()) {
            log.debug("[LIVE_REFRESH] 다른 인스턴스가 이미 갱신 중 — 스킵");
            return;
        }

        try {
            doRefresh();
        } finally {
            distributedLock.unlock(LOCK_TASK_NAME, lockToken.get());
        }
    }

    /**
     * 실제 갱신 로직. 개별 경로 갱신 실패는 로그만 남기고 계속 진행합니다.
     * (한 경로 실패가 전체 갱신을 중단시키지 않도록)
     */
    private void doRefresh() {
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

        // 하나라도 갱신에 성공했으면 그래프를 무효화해 최신 실시간 가중치로 재빌드되도록 함.
        // 전부 실패했다면 무효화하지 않아 기존 그래프(직전 가중치)를 그대로 유지한다.
        if (successCount > 0) {
            optimalRouteCalculator.invalidateGraph();
            log.info("[LIVE_REFRESH] 그래프 캐시 무효화 — 다음 탐색 요청 시 최신 실시간 가중치로 재빌드됨");
        }
    }
}
