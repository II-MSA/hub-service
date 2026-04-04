package org.iimsa.hub_service.hubroute.application.service;

import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;
import org.springframework.data.domain.Page;

public interface HubRouteApplicationService {

    HubRouteResult findHubRoute(FindHubRouteQuery query);

    Page<HubRouteResult> listHubRoutes(ListHubRouteQuery query);
}
