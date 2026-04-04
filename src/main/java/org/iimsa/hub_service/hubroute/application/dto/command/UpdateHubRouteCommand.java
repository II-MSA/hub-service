package org.iimsa.hub_service.hubroute.application.dto.command;

public record UpdateHubRouteCommand(
        Double estimatedDistance,  // km
        Integer estimatedDuration  // 분
) {
}
