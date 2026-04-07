package org.iimsa.hub_service.hub.domain.exception;

import java.util.UUID;
import org.iimsa.common.exception.ConflictException;

public class ProductDuplicatedException extends ConflictException {
    public ProductDuplicatedException(UUID productId) {
        super("이미 등록된 상품 코드입니다. - Code: " + productId);
    }
}
