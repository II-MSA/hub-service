package org.iimsa.hub_service.hub.domain.repository;

import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.iimsa.hub_service.hub.domain.model.HubProduct;

public interface HubBulkRepository {

    // 1. 업체(Company) 정보가 변경되었을 때, 허브 소속 상품(HubProduct) 내의 업체 정보 일괄 업데이트
    long bulkUpdateCompanyInfo(Company company);

    // 2. 상품(Product) 정보가 변경되었을 때, 허브 소속 상품(HubProduct) 내의 상품 상세 정보 일괄 업데이트
    long bulkUpdateProductInfo(HubProduct hubProduct);

    // 3. 허브 담당자(HubManager)의 이름 등이 변경되었을 때, 허브(Hub) 내의 담당자 정보 일괄 업데이트
    long bulkUpdateHubManagerInfo(UUID hubManagerId, String newHubManagerName);

}
