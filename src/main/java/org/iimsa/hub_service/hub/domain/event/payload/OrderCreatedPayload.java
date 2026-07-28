package org.iimsa.hub_service.hub.domain.event.payload;

import java.util.UUID;

public record OrderCreatedPayload(
        String correlationId,
        UUID orderId,
        UUID productId,
        Integer productQuantity,
        String productName,
        UUID receiverId,
        String receiverName,
        UUID receiverHubId,
        String receiverHubName,
        UUID supplierId,
        String supplierName,
        UUID supplierHubId,
        String supplierHubName,
        UUID deliveryId,
        String requestDetails
) {
}
