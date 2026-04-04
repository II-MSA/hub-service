package org.iimsa.hub_service.hubroute.application.service;

import lombok.RequiredArgsConstructor;
import org.iimsa.common.exception.NotFoundException;
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
    public HubRouteResult deleteHubRoute(UUID hubRouteId) {
        HubRoute hubRoute = hubRouteRepository.findActiveById(hubRouteId)
                .orElseThrow(() -> new NotFoundException("허브 경로를 찾을 수 없습니다."));
        hubRoute.softDelete(null);
        return HubRouteResult.from(hubRouteRepository.save(hubRoute));
    }
}
