package org.iimsa.hub_service.hubroute.application.service;

import org.iimsa.hub_service.hubroute.application.dto.command.CreateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.command.UpdateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;
import org.iimsa.hub_service.hubroute.domain.model.HubRoutePath;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface HubRouteApplicationService {

    HubRouteResult createHubRoute(CreateHubRouteCommand command);

    HubRouteResult findHubRoute(FindHubRouteQuery query);

    Page<HubRouteResult> listHubRoutes(ListHubRouteQuery query);

    HubRouteResult updateHubRoute(UUID hubRouteId, UpdateHubRouteCommand command);

    HubRouteResult deleteHubRoute(UUID hubRouteId);

    /**
     * 출발 허브 → 도착 허브 간 최적 전체 경로 조회
     * Hub 서비스가 Feign으로 호출합니다.
     */
    HubRoutePath findOptimalRoute(FindHubRoutePathQuery query);
}
