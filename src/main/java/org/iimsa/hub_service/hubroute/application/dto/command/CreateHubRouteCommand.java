package org.iimsa.hub_service.hubroute.application.dto.command;

import java.util.UUID;

/**
 * 허브 이름은 Hub 서비스 Feign 호출로 자동 조회합니다.
 * 요청 측에서 직접 입력하지 않습니다.
 */
public record CreateHubRouteCommand(
        UUID fromHubId,
        UUID toHubId,
        Double estimatedDistance,  // km  (null 허용 — 최적 경로 알고리즘 적용 전 수동 입력)
        Integer estimatedDuration  // 분  (null 허용 — 최적 경로 알고리즘 적용 전 수동 입력)
) {
}
