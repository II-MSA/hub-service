package org.iimsa.hub_service.hub.infrastructure.event;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.event.Events;
import org.iimsa.hub_service.hub.domain.event.HubEvents;
import org.iimsa.hub_service.hub.domain.event.payload.HubAction;
import org.iimsa.hub_service.hub.domain.event.payload.HubChangedPayload;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.infrastructure.messaging.kafka.HubTopicProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(HubTopicProperties.class)
public class HubEventsImpl implements HubEvents {

    private final HubTopicProperties properties;

    @Override
    public void hubChanged(Hub hub, HubAction action) {

        HubChangedPayload payload = HubChangedPayload.of(hub, action);

        log.info("[Event Publish] 허브 변경 이벤트 발행 - HubId: {}, Action: {}", hub.getId().id(), action);

        Events.trigger(
                UUID.randomUUID().toString(),
                "HUB",
                action.name(),
                properties.hubChanged(),
                payload
        );
    }
}
