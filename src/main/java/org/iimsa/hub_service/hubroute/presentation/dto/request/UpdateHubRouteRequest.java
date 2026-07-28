package org.iimsa.hub_service.hubroute.presentation.dto.request;

import jakarta.validation.constraints.Positive;
import org.iimsa.hub_service.hubroute.application.dto.command.UpdateHubRouteCommand;

public record UpdateHubRouteRequest(

        @Positive(message = "예상 거리는 양수여야 합니다.")
        Double estimatedDistance,

        @Positive(message = "예상 소요 시간은 양수여야 합니다.")
        Integer estimatedDuration
) {
    public UpdateHubRouteCommand toCommand() {
        return new UpdateHubRouteCommand(estimatedDistance, estimatedDuration);
    }
}
