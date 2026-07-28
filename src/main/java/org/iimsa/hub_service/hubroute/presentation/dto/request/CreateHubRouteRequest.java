package org.iimsa.hub_service.hubroute.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.iimsa.hub_service.hubroute.application.dto.command.CreateHubRouteCommand;

import java.util.UUID;

/**
 * 허브 이름은 Hub 서비스 Feign 호출로 자동 조회하므로 요청 필드에서 제거합니다.
 */
public record CreateHubRouteRequest(

        @NotNull(message = "출발 허브 ID는 필수입니다.")
        UUID fromHubId,

        @NotNull(message = "도착 허브 ID는 필수입니다.")
        UUID toHubId,

        // 최적 경로 알고리즘 미구현 상태에서는 수동 입력 허용 (nullable)
        @Positive(message = "예상 거리는 양수여야 합니다.")
        Double estimatedDistance,

        @Positive(message = "예상 소요 시간은 양수여야 합니다.")
        Integer estimatedDuration
) {
    public CreateHubRouteCommand toCommand() {
        return new CreateHubRouteCommand(
                fromHubId,
                toHubId,
                estimatedDistance,
                estimatedDuration
        );
    }
}
