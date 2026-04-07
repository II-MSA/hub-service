package org.iimsa.hub_service.hub.domain.service;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.CompanyData;

public interface CompanyProvider {
    CompanyData getCompany(UUID id);
}
