package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubDeliveryManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {
    @GetMapping("/users/next-sequence/hub-delivery")
    CommonResponse<HubDeliveryManagerResponse> getNextHubDeliveryManager(@RequestParam("hubId") UUID hubId);
}
