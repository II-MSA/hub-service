package org.iimsa.hub_service.hubroute.presentation.dto.response;

import org.iimsa.hub_service.hubroute.application.dto.result.HubRouteResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record HubRouteResponse(
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
    public static HubRouteResponse from(HubRouteResult result) {
        return new HubRouteResponse(
                result.id(),
                result.fromHubId(),
                result.fromHubName(),
                result.toHubId(),
                result.toHubName(),
                result.estimatedDistance(),
                result.estimatedDuration(),
                result.createdAt(),
                result.modifiedAt()
        );
    }
}
