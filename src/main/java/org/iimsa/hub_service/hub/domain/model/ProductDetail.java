package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.iimsa.common.exception.BadRequestException;
import org.iimsa.hub_service.hub.domain.exception.ProductDuplicatedException;
import org.iimsa.hub_service.hub.domain.exception.ProductNotFoundException;
import org.iimsa.hub_service.hub.domain.service.ProductProvider;
import org.iimsa.hub_service.hub.domain.service.dto.ProductData;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDetail {
    @Column(name="product_id", nullable = false)
    private UUID productId;

    @Column(length=150, name="product_name")
    private String name;

    protected ProductDetail(UUID productId, ProductProvider provider) {

        if (provider == null) {
            throw new BadRequestException("업체 상품 Provider가 누락되었습니다.");
        }

        // 중복 상품 여부 체크
        if (provider.isDuplicated(productId)) {
            throw new ProductDuplicatedException(productId);
        }

        ProductData data = provider.getProduct(productId);
        if (data == null || !StringUtils.hasText(data.name())) {
            throw new ProductNotFoundException(productId);
        }

        this.productId = productId;
        this.name = data.name();
    }
}
