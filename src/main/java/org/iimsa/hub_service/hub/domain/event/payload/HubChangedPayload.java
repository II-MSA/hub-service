package org.iimsa.hub_service.hub.domain.event.payload;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.Hub;

public record HubChangedPayload(
        UUID hubId,
        String hubName,
        String address,
        Double latitude,
        Double longitude,
        HubAction action
) {
    public static HubChangedPayload of(Hub hub, HubAction action) {
        if (hub.getAddress() == null || hub.getAddress().getAddress() == null) {
            throw new IllegalStateException("Hub address is null");
        }

        return new HubChangedPayload(
                hub.getId().id(),
                hub.getName(),
                hub.getAddress().getAddress(),
                hub.getAddress().getLatitude(),
                hub.getAddress().getLongitude(),
                action
        );
    }
}
