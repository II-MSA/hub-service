package org.iimsa.hub_service.hubroute.application.service;

import lombok.RequiredArgsConstructor;
import org.ticketing.common.exception.ConflictException;
import org.ticketing.common.exception.NotFoundException;
import org.iimsa.hub_service.hubroute.application.dto.command.CreateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.command.UpdateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.model.HubRouteEdgeKey;
import org.iimsa.hub_service.hubroute.domain.model.HubRoutePath;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery.Algorithm;
import org.iimsa.hub_service.hubroute.domain.service.OptimalRouteCalculator;
import org.iimsa.hub_service.hubroute.domain.service.OptimalRouteCalculator.RouteCalculationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteApplicationServiceImpl implements HubRouteApplicationService {

    private final HubRouteRepository hubRouteRepository;
    private final HubInfoRepository hubInfoRepository;
    private final HubRouteCacheRepository hubRouteCacheRepository;
    private final OptimalRouteCalculator optimalRouteCalculator;

    /**
     * buildAndWarmCache() 동시 실행을 막기 위한 락.
     *
     * <p>needsRebuild() 확인 후 buildAndWarmCache() 호출 사이의 시간차 동안 다수의
     * 스레드가 동시에 캐시 미적재 상태를 관찰할 수 있습니다. 이 락이 없으면 각 스레드가
     * 전부 DB 조회 + Feign 배치 호출을 중복 수행하게 되어(예: VU=200 동시 유입 시
     * 수십~수백 회 중복 재빌드) Tomcat 스레드풀/HikariCP 커넥션풀이 자기 자신의
     * 부하로 고갈되는 문제가 있었습니다.
     */
    private final Object cacheBuildLock = new Object();

    @Override
    @Transactional
    public HubRouteResult createHubRoute(CreateHubRouteCommand command) {
        if (hubRouteRepository.existsByFromHubIdAndToHubId(command.fromHubId(), command.toHubId())) {
            throw new ConflictException("이미 존재하는 허브 경로입니다.");
        }

        // Hub 서비스 Feign 호출 — 허브명 자동 조회
        HubInfo fromHub = hubInfoRepository.findHub(command.fromHubId());
        HubInfo toHub   = hubInfoRepository.findHub(command.toHubId());

        HubRoute hubRoute = HubRoute.builder()
                .fromHubId(command.fromHubId())
                .fromHubName(fromHub.name())
                .toHubId(command.toHubId())
                .toHubName(toHub.name())
                .estimatedDistance(command.estimatedDistance())
                .estimatedDuration(command.estimatedDuration())
                .build();

        HubRouteResult result = HubRouteResult.from(hubRouteRepository.save(hubRoute));
        optimalRouteCalculator.invalidateGraph();
        return result;
    }

    @Override
    public HubRouteResult findHubRoute(FindHubRouteQuery query) {
        HubRoute hubRoute = hubRouteRepository.findActiveById(query.hubRouteId())
                .orElseThrow(() -> new NotFoundException("허브 경로를 찾을 수 없습니다."));
        return HubRouteResult.from(hubRoute);
    }

    @Override
    public Page<HubRouteResult> listHubRoutes(ListHubRouteQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());

        if (query.fromHubId() != null) {
            return hubRouteRepository
                    .findAllActiveByFromHubId(query.fromHubId(), pageRequest)
                    .map(HubRouteResult::from);
        }

        return hubRouteRepository.findAllActive(pageRequest).map(HubRouteResult::from);
    }

    @Override
    @Transactional
    public HubRouteResult updateHubRoute(UUID hubRouteId, UpdateHubRouteCommand command) {
        HubRoute hubRoute = hubRouteRepository.findActiveById(hubRouteId)
                .orElseThrow(() -> new NotFoundException("허브 경로를 찾을 수 없습니다."));
        hubRoute.update(command.estimatedDistance(), command.estimatedDuration());
        HubRouteResult result = HubRouteResult.from(hubRouteRepository.save(hubRoute));
        optimalRouteCalculator.invalidateGraph();
        return result;
    }

    @Override
    @Transactional
    public HubRouteResult deleteHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = hubRouteRepository.findActiveById(hubRouteId)
                .orElseThrow(() -> new NotFoundException("허브 경로를 찾을 수 없습니다."));
        hubRoute.softDelete(null);
        HubRouteResult result = HubRouteResult.from(hubRouteRepository.save(hubRoute));
        optimalRouteCalculator.invalidateGraph();
        return result;
    }

    @Override
    public HubRoutePath findOptimalRoute(FindHubRoutePathQuery query) {
        // 캐시 미적재 시에만 DB·Feign 호출 — 캐시 히트 시 I/O 없음
        if (optimalRouteCalculator.needsRebuild()) {
            buildAndWarmCache();
        }

        Algorithm algorithm = query.algorithm() != null ? query.algorithm() : Algorithm.ASTAR;

        RouteCalculationResult result = algorithm == Algorithm.DIJKSTRA
                ? optimalRouteCalculator.calculateDijkstra(query.originHubId(), query.destinationHubId())
                : optimalRouteCalculator.calculate(query.originHubId(), query.destinationHubId());

        if (result.path().isEmpty()) {
            throw new NotFoundException("출발 허브에서 도착 허브까지의 경로를 찾을 수 없습니다.");
        }

        return HubRoutePath.of(query.originHubId(), query.destinationHubId(),
                result.path(), result.nodesExplored());
    }

    // ── private helpers ──────────────────────────────────────────────────────────

    /**
     * 전체 허브 경로를 DB에서 읽고, 참조된 모든 허브 좌표를 Feign 으로 일괄 조회하고,
     * 각 구간의 실시간 소요시간을 Redis 에서 벌크 조회한 뒤 A* 그래프 캐시를 재빌드합니다.
     *
     * <p>cacheBuildLock 으로 감싸고, 락 진입 직후 needsRebuild() 를 다시 확인합니다
     * (이중 체크 로킹). needsRebuild() 확인 후 이 메서드를 호출하는 사이에 다른
     * 스레드가 먼저 락을 잡고 재빌드를 끝냈다면, 뒤이어 락을 잡는 스레드들은
     * DB 조회·Feign 호출·Redis 조회 없이 즉시 반환합니다.
     *
     * <p>Redis 조회는 구간 수만큼 개별 호출하지 않고 {@link HubRouteCacheRepository#getLiveBulk}로
     * 한 번에 가져옵니다 — 탐색 중 개별 조회와 동일한 N+1 패턴을 그래프 빌드 시점에
     * 재현하지 않기 위함입니다.
     */
    private void buildAndWarmCache() {
        synchronized (cacheBuildLock) {
            if (!optimalRouteCalculator.needsRebuild()) {
                return; // 락 대기 중 다른 스레드가 이미 재빌드를 완료함
            }
            List<HubRoute> allRoutes = hubRouteRepository.findAllActive();
            Set<UUID> hubIds = extractHubIds(allRoutes);
            Map<UUID, HubInfo> hubCoords = hubInfoRepository.findHubsByIds(hubIds);
            Map<HubRouteEdgeKey, LiveRouteCache> liveWeights = fetchLiveWeights(allRoutes);
            optimalRouteCalculator.warmCache(allRoutes, hubCoords, liveWeights);
        }
    }

    /**
     * 경로 목록에서 참조된 모든 허브 ID(출발 + 도착)를 추출합니다.
     */
    private Set<UUID> extractHubIds(List<HubRoute> routes) {
        return routes.stream()
                .flatMap(r -> Stream.of(r.getFromHubId(), r.getToHubId()))
                .collect(Collectors.toSet());
    }

    /**
     * 경로 목록의 각 구간(from→to)에 대한 Redis 실시간 소요시간을 벌크 조회합니다.
     * 캐시 미스인 구간은 결과 맵에서 제외되며, {@link OptimalRouteCalculator}가
     * DB {@code estimatedDuration}/{@code estimatedDistance}로 폴백합니다.
     */
    private Map<HubRouteEdgeKey, LiveRouteCache> fetchLiveWeights(List<HubRoute> routes) {
        Set<HubRouteEdgeKey> edgeKeys = routes.stream()
                .map(r -> HubRouteEdgeKey.of(r.getFromHubId(), r.getToHubId()))
                .collect(Collectors.toSet());
        return hubRouteCacheRepository.getLiveBulk(edgeKeys);
    }
}
