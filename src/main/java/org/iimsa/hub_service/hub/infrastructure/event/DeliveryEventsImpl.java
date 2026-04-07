package org.iimsa.hub_service.hub.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.event.Events;
import org.iimsa.hub_service.hub.domain.event.DeliveryEvents;
import org.iimsa.hub_service.hub.domain.event.payload.DeliveryRequestedPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventsImpl implements DeliveryEvents {

    @Value("${topics.delivery.requested:delivery-requested-topic}")
    private String deliveryRequestedTopic;

    @Override
    public void publishDeliveryRequest(DeliveryRequestedPayload payload) {
        log.info("[Event Publish] 배달 요청 이벤트 발행 - OrderId: {}, DeliveryId: {}",
                payload.orderId(), payload.deliveryId());

        Events.trigger(
                payload.correlationId(),
                "DELIVERY",
                "REQUESTED",
                deliveryRequestedTopic,
                payload
        );
    }
}
