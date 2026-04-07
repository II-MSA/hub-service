package org.iimsa.hub_service.hub.infrastructure.persistence;

import static org.iimsa.hub_service.hub.domain.model.QHub.hub;
import static org.iimsa.hub_service.hub.domain.model.QHubProduct.hubProduct;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.iimsa.hub_service.hub.domain.model.HubProduct;
import org.iimsa.hub_service.hub.domain.repository.HubBulkRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubBulkRepositoryImpl implements HubBulkRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long bulkUpdateCompanyInfo(Company company) {
        return queryFactory.update(hubProduct)
                .set(hubProduct.company.name, company.getName())
                .set(hubProduct.modifiedAt, LocalDateTime.now())
                .set(hubProduct.modifiedBy, "SYSTEM")
                .where(hubProduct.company.id.eq(company.getId()))
                .execute();
    }

    @Override
    public long bulkUpdateProductInfo(HubProduct targetProduct) {
        return queryFactory.update(hubProduct)
                .set(hubProduct.detail.name, targetProduct.getDetail().getName())
                .set(hubProduct.modifiedAt, LocalDateTime.now())
                .set(hubProduct.modifiedBy, "SYSTEM")
                .where(hubProduct.detail.productId.eq(targetProduct.getDetail().getProductId()))
                .execute();
    }

    @Override
    public long bulkUpdateHubManagerInfo(UUID hubManagerId, String newHubManagerName) {
        return queryFactory.update(hub)
                .set(hub.hubManager.hubManagerName, newHubManagerName)
                .set(hub.modifiedAt, LocalDateTime.now())
                .set(hub.modifiedBy, "SYSTEM")
                .where(hub.hubManager.hubManagerId.eq(hubManagerId))
                .execute();
    }
}
