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
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.model.HubRoutePath;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
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
    private final OptimalRouteCalculator optimalRouteCalculator;

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
     * 전체 허브 경로를 DB에서 읽고, 참조된 모든 허브 좌표를 Feign 으로 일괄 조회한 뒤
     * A* 그래프 캐시를 재빌드합니다.
     *
     * <p>needsRebuild() 확인 후 이 메서드를 호출하는 사이에 다른 스레드가 먼저
     * warmCache 를 완료할 수 있습니다. warmCache 내부 DCL 이 이중 빌드를 막으므로
     * 중복 호출은 안전하며 성능상 무해합니다.
     */
    private void buildAndWarmCache() {
        List<HubRoute> allRoutes = hubRouteRepository.findAllActive();
        Set<UUID> hubIds = extractHubIds(allRoutes);
        Map<UUID, HubInfo> hubCoords = hubInfoRepository.findHubsByIds(hubIds);
        optimalRouteCalculator.warmCache(allRoutes, hubCoords);
    }

    /**
     * 경로 목록에서 참조된 모든 허브 ID(출발 + 도착)를 추출합니다.
     */
    private Set<UUID> extractHubIds(List<HubRoute> routes) {
        return routes.stream()
                .flatMap(r -> Stream.of(r.getFromHubId(), r.getToHubId()))
                .collect(Collectors.toSet());
    }
}
