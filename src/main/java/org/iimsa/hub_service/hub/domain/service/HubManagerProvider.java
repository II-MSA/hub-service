package org.iimsa.hub_service.hub.domain.service;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.HubManagerData;

public interface HubManagerProvider {
    HubManagerData getHubManager(UUID hubManagerId);
}
