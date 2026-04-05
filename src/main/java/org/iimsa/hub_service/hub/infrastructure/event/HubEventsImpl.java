package org.iimsa.hub_service.hub.infrastructure.event;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.common.event.Events;
import org.iimsa.hub_service.hub.domain.event.HubEvents;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.infrastructure.messaging.kafka.HubTopicProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(HubTopicProperties.class)
public class HubEventsImpl implements HubEvents {

    private final HubTopicProperties properties;

    @Override
    public void hubChanged(Hub hub, boolean reRoute) {

        record HubChangedPayload(
                UUID hubId,
                String name,
                boolean reRoute
        ) {}

        HubChangedPayload payload = new HubChangedPayload(
                hub.getId().id(),
                hub.getName(),
                reRoute
        );

        Events.trigger(UUID.randomUUID().toString(), "HUB", "UPDATED", properties.hubChanged(), payload);
    }
}
