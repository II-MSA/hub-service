package org.iimsa.hub_service.hubroute.application.dto.result;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;

import java.time.LocalDateTime;
import java.util.UUID;

public record HubRouteResult(
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
    public static HubRouteResult from(HubRoute hubRoute) {
        return new HubRouteResult(
                hubRoute.getId(),
                hubRoute.getFromHubId(),
                hubRoute.getFromHubName(),
                hubRoute.getToHubId(),
                hubRoute.getToHubName(),
                hubRoute.getEstimatedDistance(),
                hubRoute.getEstimatedDuration(),
                hubRoute.getCreatedAt(),
                hubRoute.getModifiedAt()
        );
    }
}
