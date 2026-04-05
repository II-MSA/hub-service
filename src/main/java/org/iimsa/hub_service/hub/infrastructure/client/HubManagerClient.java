package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "hub-service",
        fallbackFactory = HubManagerClientFallbackFactory.class
)
public interface HubManagerClient {
    @GetMapping("/users/{userId}")
    HubManagerResponse getHubManager(@PathVariable("userId") UUID id);
}
