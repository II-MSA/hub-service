package org.iimsa.hub_service.hubroute.infrastructure.feign;

import java.util.UUID;

/**
 * Hub 서비스 Feign 응답 DTO
 * GET /api/v1/hubs/{hubId} 응답의 data 필드에 매핑
 */
public record HubFeignResponse(
        UUID id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
}
