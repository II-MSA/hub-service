package org.iimsa.hub_service.hubroute.application.dto.query;

import java.util.UUID;

public record FindHubRoutePathQuery(
        UUID originHubId,
        UUID destinationHubId
) {
}
