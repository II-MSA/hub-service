package org.iimsa.hub_service.hub.infrastructure.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "topics.hub")
public record HubTopicProperties(
        String hubChanged
) {}
