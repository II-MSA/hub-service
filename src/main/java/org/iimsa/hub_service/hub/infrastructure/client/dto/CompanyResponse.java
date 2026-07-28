package org.iimsa.hub_service.hub.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyResponse (
        UUID id,
        String name
) {
}
