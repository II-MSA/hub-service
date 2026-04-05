package org.iimsa.hub_service.hub.domain.exception;

import java.util.UUID;
import org.iimsa.common.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(UUID productId) {
        super("상품을 찾을 수 없습니다. - Product ID: %s".formatted(productId));
    }
}
