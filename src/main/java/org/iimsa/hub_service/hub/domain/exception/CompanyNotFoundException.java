package org.iimsa.hub_service.hub.domain.exception;

import java.util.UUID;
import org.iimsa.common.exception.NotFoundException;

public class CompanyNotFoundException extends NotFoundException {
    public CompanyNotFoundException(UUID id) {
        super("업체를 찾을수 없습니다. ID: " + id);
    }
}
