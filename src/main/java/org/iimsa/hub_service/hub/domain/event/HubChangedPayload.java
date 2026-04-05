package org.iimsa.hub_service.hub.domain.event;

import java.util.List;
import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.Address;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubManager;
import org.iimsa.hub_service.hub.domain.model.HubProduct;

public record HubChangedPayload(
        UUID hubId,
        String hubName,
        String address,
        Double latitude,
        Double longitude,
        List<HubProduct> products,
        UUID hubManagerId,
        String hubManagerName
) {
    public static HubChangedPayload from(Hub hub,HubManager hubManager) {

        Address address = hub.getAddress();

        if (address == null || address.getAddress() == null || hubManager == null) {
            throw new IllegalStateException("Hub address or hubManager is null");
        }

        return new HubChangedPayload(
                hub.getId().id(),
                hub.getName(),
                hub.getAddress().getAddress(),
                hub.getAddress().getLatitude(),
                hub.getAddress().getLongitude(),
                hub.getProducts(),
                hubManager.getHubManagerId(),
                hubManager.getHubManagerName()
        );
    }
}
