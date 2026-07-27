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
     *
     * <p>hub 도메인의 {@code HubRouteProviderImpl} 이 같은 프로세스 내에서 직접 호출합니다
     * (hubroute 가 물리적으로 분리되기 전까지는 Feign 을 거치지 않습니다).
     */
    HubRoutePath findOptimalRoute(FindHubRoutePathQuery query);
}
