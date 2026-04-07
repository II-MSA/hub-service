package org.iimsa.hub_service.hub.domain.event;

import org.iimsa.hub_service.hub.domain.event.payload.HubAction;
import org.iimsa.hub_service.hub.domain.model.Hub;

public interface HubEvents {
    void hubChanged(Hub hub, HubAction action);
}
