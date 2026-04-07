package org.iimsa.hub_service.hub.infrastructure.client.dto;

import java.util.UUID;

public record HubDeliveryManagerResponse(
        UUID deliverId,
        String name,
        String slackId,
        Integer nextSequence
) {
}
