package org.iimsa.hub_service.hub.domain.service;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.HubRoutePathData;

public interface HubRouteProvider {
    HubRoutePathData getHubRoute(UUID startHubId, UUID endHubId);
}
