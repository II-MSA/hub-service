package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.iimsa.hub_service.hub.infrastructure.client.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service"
)
public interface ProductClient {
    @GetMapping("/products/{productId}")
    ProductResponse getProduct(@PathVariable("productId") UUID storeId);
}
