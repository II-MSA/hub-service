package org.iimsa.hub_service.hub.domain.service;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.service.dto.ProductData;

public interface ProductProvider {
    ProductData getProduct(UUID hubProductId);
    boolean isDuplicated(UUID companyId);
}
