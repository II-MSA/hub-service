package org.iimsa.hub_service.hubroute.application.service;

import lombok.RequiredArgsConstructor;
import org.iimsa.common.exception.ConflictException;
import org.iimsa.common.exception.NotFoundException;
import org.iimsa.hub_service.hubroute.application.dto.command.CreateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.command.UpdateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteApplicationServiceImpl implements HubRouteApplicationService {

    private final HubRouteRepository hubRouteRepository;

    @Override
    @Transactional
    public HubRouteResult createHubRoute(CreateHubRouteCommand command) {
        if (hubRouteRepository.existsByFromHubIdAndToHubId(command.fromHubId(), command.toHubId())) {
            throw new ConflictException("이미 존재하는 허브 경로입니다.");
        }

        // TODO: 최적 경로 알고리즘 적용 — estimatedDistance, estimatedDuration 자동 계산
        //       현재는 요청값을 그대로 사용 (수동 입력)

        HubRoute hubRoute = HubRoute.builder()
                .fromHubId(command.fromHubId())
                .fromHubName(command.fromHubName())
                .toHubId(command.toHubId())
                .toHubName(command.toHubName())
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
}
