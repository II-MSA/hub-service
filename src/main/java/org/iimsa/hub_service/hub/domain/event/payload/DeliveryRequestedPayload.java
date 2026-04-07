package org.iimsa.hub_service.hub.domain.event.payload;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.HubDeliveryManagerData;
import org.iimsa.hub_service.hub.domain.service.dto.HubRoutePathData;

public record DeliveryRequestedPayload(
        String correlationId,
        UUID orderId,
        UUID deliveryId,

        UUID productId,
        String productName,
        Integer productQuantity,

        UUID supplierId,
        String supplierName,
        UUID startHubId,
        String startHubName,

        UUID receiverId,
        String receiverName,
        UUID endHubId,
        String endHubName,

        UUID hubDeliveryManagerId,
        String hubDeliveryManagerName,
        String hubDeliveryManagerSlackId,
        Integer hubDeliveryManagerSequence,

        Integer totalEstimatedDuration,
        Double totalEstimatedDistance,
        HubRoutePathData routePath,

        String requestDetails
) {
    public static DeliveryRequestedPayload of(OrderCreatedPayload order, HubRoutePathData routePath, HubDeliveryManagerData driverData) {
        return new DeliveryRequestedPayload(
                order.correlationId(),
                order.orderId(),
                order.deliveryId(),

                order.productId(),
                order.productName(),
                order.productQuantity(),

                order.supplierId(),
                order.supplierName(),
                order.supplierHubId(),
                order.supplierHubName(),

                order.receiverId(),
                order.receiverName(),
                order.receiverHubId(),
                order.receiverHubName(),

                driverData.deliveryManagerId(),
                driverData.name(),
                driverData.slackId(),
                driverData.sequence(),

                routePath.totalDuration(),
                routePath.totalDistance(),
                routePath,

                order.requestDetails()
        );
    }
}
