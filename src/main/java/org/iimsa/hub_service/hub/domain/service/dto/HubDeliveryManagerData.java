package org.iimsa.hub_service.hub.domain.service.dto;

import java.util.UUID;

public record HubDeliveryManagerData(
        UUID deliveryManagerId,
        String name,
        String slackId,
        Integer sequence
) {
}
