package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;
import org.iimsa.common.domain.BaseEntity;
import org.iimsa.common.exception.BadRequestException;
import org.iimsa.hub_service.hub.domain.service.CompanyProvider;
import org.iimsa.hub_service.hub.domain.service.ProductProvider;

@Getter
@Entity
@ToString
@Table(
        name="P_HUB_PRODUCT",
        indexes = {
                @Index(name = "idx_hub_product_company_code_deleted", columnList = "company_id, product_code, deleted_at"),
                @Index(name = "idx_hub_product_hub_id", columnList = "hub_id"),
                @Index(name = "idx_hub_product_status", columnList = "status")
        }
)
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubProduct extends BaseEntity {
    @EmbeddedId
    private ProductId id;

    @Embedded
    private ProductDetail detail;

    @Column(name="item_stock")
    private int stock;

    @Embedded
    private Company company;

    @Builder
    protected HubProduct(int stock, UUID companyId, UUID productId, CompanyProvider companyProvider, ProductProvider productProvider) {

        if (stock < 0) {
            throw new BadRequestException("재고 수량은 음수일 수 없습니다.");
        }

        this.id = ProductId.of();
        this.stock = stock;

        this.detail = new ProductDetail(productId, productProvider);
        this.company = new Company(companyId, companyProvider);

    }

    // reduce Stock
    public void reduceStock(int quantity) {
        if (this.stock < quantity) {
            throw new BadRequestException("재고가 부족합니다. 현재 재고: " + this.stock);
        }
        this.stock -= quantity;

    }

    // add Stock
    public void addStock(int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("추가할 재고는 1개 이상이어야 합니다.");
        }
        this.stock += quantity;
    }

    // delete product
    public void delete() {
        super.delete(null);
    }

}
