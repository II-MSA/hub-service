package org.iimsa.hub_service.hubroute.application.service;

import org.iimsa.hub_service.hubroute.application.dto.command.UpdateHubRouteCommand;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface HubRouteApplicationService {

    HubRouteResult findHubRoute(FindHubRouteQuery query);

    Page<HubRouteResult> listHubRoutes(ListHubRouteQuery query);

    HubRouteResult updateHubRoute(UUID hubRouteId, UpdateHubRouteCommand command);

    HubRouteResult deleteHubRoute(UUID hubRouteId);
}
