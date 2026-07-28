package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.ticketing.common.response.CommonResponse;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * user-service 의 사용자(허브 관리자) 조회 Feign 클라이언트
 *
 * <p>기존에는 {@code name = "hub-service"} 로 잘못 지정되어 있어 hub-service가
 * 자기 자신에게 존재하지도 않는 {@code /users/{userId}} 경로를 호출하는 버그였습니다
 * (fallback 로그/예외 메시지에는 "User Service"라고 되어 있어 원래 의도는 user-service
 * 호출이었음이 명확합니다). user-service의 실제 컨트롤러({@code UserController},
 * {@code @RequestMapping("/api/v1/users")}) 경로와 응답 타입({@code CommonResponse<Info>})에
 * 맞춰 name·경로·반환 타입을 모두 수정했습니다.
 *
 * <p>참고: 같은 패키지의 {@link UserClient} 는 경로에 {@code /api/v1} 접두사가 빠져 있어
 * (예: {@code /users/next-sequence/hub-delivery}) 동일한 문제를 갖고 있을 수 있으나,
 * 이번 수정 범위에는 포함하지 않았습니다.
 */
@FeignClient(
        name = "user-service",
        fallbackFactory = HubManagerClientFallbackFactory.class
)
public interface HubManagerClient {
    @GetMapping("/api/v1/users/{userId}")
    CommonResponse<HubManagerResponse> getHubManager(@PathVariable("userId") UUID id);
}
