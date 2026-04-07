package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubRoutePathResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "hub-service",
        fallbackFactory = HubRouteClientFallbackFactory.class
)
public interface HubRouteClient {
    @GetMapping("/path")
    CommonResponse<HubRoutePathResponse> findOptimalRoute(@RequestParam("originHubId") UUID originHubId, @RequestParam("destinationHubId") UUID destinationHubId);
}
