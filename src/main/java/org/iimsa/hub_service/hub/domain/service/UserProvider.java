package org.iimsa.hub_service.hub.domain.service;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.HubDeliveryManagerData;

public interface UserProvider {
    HubDeliveryManagerData getNextHubDeliveryManager(UUID hubId);
}
