package org.iimsa.hub_service.hubroute.infrastructure.feign;

import java.util.UUID;

/**
 * Hub 서비스 Feign 응답 DTO
 * GET /hubs/{hubId} 응답의 data 필드에 매핑
 *
 * <p>GetHubResponseDto.Info 의 JSON 필드명과 일치해야 합니다:
 * hubId, hubName, address, latitude, longitude
 */
public record HubFeignResponse(
        UUID hubId,
        String hubName,
        String address,
        Double latitude,
        Double longitude
) {
}
