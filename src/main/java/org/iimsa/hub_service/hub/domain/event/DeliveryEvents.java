package org.iimsa.hub_service.hub.domain.event;

import org.iimsa.hub_service.hub.domain.event.payload.DeliveryRequestedPayload;

public interface DeliveryEvents {
    void publishDeliveryRequest(DeliveryRequestedPayload payload);
}
