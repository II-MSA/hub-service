package org.iimsa.hub_service.hub.domain.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.HubRoute;

public record HubRouteData(
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
    public static HubRouteData from(HubRoute route) {
        return new HubRouteData(
                route.getId(),
                route.getFromHubId(),
                route.getFromHubName(),
                route.getToHubId(),
                route.getToHubName(),
                route.getEstimatedDistance(),
                route.getEstimatedDuration(),
                route.getCreatedAt(),
                route.getModifiedAt()
        );
    }
}
