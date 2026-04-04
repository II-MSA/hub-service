package org.iimsa.hub_service.hubroute.application.dto.command;

import java.util.UUID;

public record CreateHubRouteCommand(
        UUID fromHubId,
        String fromHubName,
        UUID toHubId,
        String toHubName,
        Double estimatedDistance,  // km  (null 허용 — 최적 경로 알고리즘 적용 전 수동 입력)
        Integer estimatedDuration  // 분  (null 허용 — 최적 경로 알고리즘 적용 전 수동 입력)
) {
}
