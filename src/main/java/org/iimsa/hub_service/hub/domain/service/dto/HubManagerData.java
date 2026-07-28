package org.iimsa.hub_service.hub.domain.service.dto;

import java.util.UUID;

public record HubManagerData(
        UUID hubManagerId,
        String hubManagerName
) {
}
