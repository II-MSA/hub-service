package org.iimsa.hub_service.hub.domain.exception;

import org.iimsa.common.exception.ForbiddenException;
import org.iimsa.hub_service.hub.domain.model.UserType;

public class MasterOnlyException extends ForbiddenException {
    public MasterOnlyException() {
        super(UserType.MASTER.getDescription() + "만 수행할 수 있는 작업입니다.");
    }
}
