package org.iimsa.hub_service.hub.infrastructure.provider;

import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hub.domain.repository.HubQueryRepository;
import org.iimsa.hub_service.hub.domain.service.ProductProvider;
import org.iimsa.hub_service.hub.domain.service.dto.ProductData;
import org.iimsa.hub_service.hub.infrastructure.client.ProductClient;
import org.iimsa.hub_service.hub.infrastructure.client.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductProviderImpl implements ProductProvider {

    private final ProductClient client;
    private final HubQueryRepository queryRepository;

    @Override
    public ProductData getProduct(UUID hubProductId) {

        try {
            ProductResponse res = client.getProduct(hubProductId);
            return res == null || res.name() == null ? null : new ProductData(res.id(), res.name());

        } catch (FeignException.NotFound e) {
            log.warn("Hub Product를 찾을 수 없습니다. companyId={}", hubProductId);
            return null;

        } catch (FeignException e) {
            log.error("Product 서비스 호출 실패: productId={}, Error={}", hubProductId, e.getMessage());
            throw new RuntimeException("허브 상품 정보 조회 중 외부 서비스 호출에 실패했습니다.", e);
        }
    }

    @Override
    public boolean isDuplicated(UUID companyId) {
        return queryRepository.isDuplicatedItem(companyId);
    }
}
