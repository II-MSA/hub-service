package org.iimsa.hub_service.hubroute.application.service;

import lombok.RequiredArgsConstructor;
import org.iimsa.common.exception.ConflictException;
import org.iimsa.common.exception.NotFoundException;
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
import org.iimsa.hub_service.hubroute.domain.service.OptimalRouteCalculator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

        return HubRouteResult.from(hubRouteRepository.save(hubRoute));
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
        return HubRouteResult.from(hubRouteRepository.save(hubRoute));
    }

    @Override
    @Transactional
    public HubRouteResult deleteHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = hubRouteRepository.findActiveById(hubRouteId)
                .orElseThrow(() -> new NotFoundException("허브 경로를 찾을 수 없습니다."));
        hubRoute.softDelete(null);
        return HubRouteResult.from(hubRouteRepository.save(hubRoute));
    }

    @Override
    public HubRoutePath findOptimalRoute(FindHubRoutePathQuery query) {
        List<HubRoute> allRoutes = hubRouteRepository.findAllActive();

        List<HubRoute> optimalPath = optimalRouteCalculator.calculate(
                query.originHubId(), query.destinationHubId(), allRoutes
        );

        if (optimalPath.isEmpty()) {
            throw new NotFoundException("출발 허브에서 도착 허브까지의 경로를 찾을 수 없습니다.");
        }

        return HubRoutePath.of(query.originHubId(), query.destinationHubId(), optimalPath);
    }
}
