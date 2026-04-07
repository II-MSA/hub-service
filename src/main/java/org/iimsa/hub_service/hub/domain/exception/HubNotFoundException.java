package org.iimsa.hub_service.hub.domain.exception;

import java.util.UUID;
import org.iimsa.common.exception.NotFoundException;
import org.iimsa.hub_service.hub.domain.model.HubId;

public class HubNotFoundException extends NotFoundException {
    public HubNotFoundException(UUID hubId) {
        super("허브를 찾을 수 없습니다. Hub ID: " + hubId);
    }

    public HubNotFoundException(HubId hubId) {
        this(hubId.id());
    }
}
