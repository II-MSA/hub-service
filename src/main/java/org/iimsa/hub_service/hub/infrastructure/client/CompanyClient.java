package org.iimsa.hub_service.hub.infrastructure.client;

import java.util.UUID;
import org.iimsa.hub_service.hub.infrastructure.client.dto.CompanyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "company-service",
        fallbackFactory = CompanyClientFallbackFactory.class
)
public interface CompanyClient {
    @GetMapping("/{companyId}/details")
    CompanyResponse getCompany(@PathVariable("companyId") UUID id);
}
