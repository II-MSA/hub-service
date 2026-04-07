package org.iimsa.hub_service.hub.domain.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HubRouteResultData(
        UUID id,
        UUID fromHubId,
        String fromHubName,
        UUID toHubId,
        String toHubName,
        Double estimatedDistance,
        Integer estimatedDuration,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static HubRouteResultData from(HubRouteData hubRoute) {
        return new HubRouteResultData(
                hubRoute.id(),
                hubRoute.fromHubId(),
                hubRoute.fromHubName(),
                hubRoute.toHubId(),
                hubRoute.toHubName(),
                hubRoute.estimatedDistance(),
                hubRoute.estimatedDuration(),
                hubRoute.createdAt(),
                hubRoute.modifiedAt()
        );
    }
}
