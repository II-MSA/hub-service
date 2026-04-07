package org.iimsa.hub_service.hub.infrastructure.client.dto;

import java.util.UUID;

public record HubManagerResponse(
        UUID id,
        String name
) {
}
