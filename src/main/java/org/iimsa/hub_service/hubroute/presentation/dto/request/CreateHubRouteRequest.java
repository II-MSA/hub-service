package org.iimsa.hub_service.hubroute.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.iimsa.hub_service.hubroute.application.dto.command.CreateHubRouteCommand;

import java.util.UUID;

public record CreateHubRouteRequest(

        @NotNull(message = "출발 허브 ID는 필수입니다.")
        UUID fromHubId,

        @NotBlank(message = "출발 허브명은 필수입니다.")
        String fromHubName,

        @NotNull(message = "도착 허브 ID는 필수입니다.")
        UUID toHubId,

        @NotBlank(message = "도착 허브명은 필수입니다.")
        String toHubName,

        // 최적 경로 알고리즘 미구현 상태에서는 수동 입력 허용 (nullable)
        @Positive(message = "예상 거리는 양수여야 합니다.")
        Double estimatedDistance,

        @Positive(message = "예상 소요 시간은 양수여야 합니다.")
        Integer estimatedDuration
) {
    public CreateHubRouteCommand toCommand() {
        return new CreateHubRouteCommand(
                fromHubId,
                fromHubName,
                toHubId,
                toHubName,
                estimatedDistance,
                estimatedDuration
        );
    }
}
