package org.iimsa.hub_service.hub.application.service;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.exception.UnAuthorizedException;
import org.iimsa.common.util.SecurityUtil;
import org.iimsa.hub_service.hub.application.dto.HubServiceDto;
import org.iimsa.hub_service.hub.domain.event.HubEvents;
import org.iimsa.hub_service.hub.domain.exception.HubNotFoundException;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubProduct;
import org.iimsa.hub_service.hub.domain.model.ProductId;
import org.iimsa.hub_service.hub.domain.repository.HubBulkRepository;
import org.iimsa.hub_service.hub.domain.repository.HubRepository;
import org.iimsa.hub_service.hub.domain.service.AddressResolver;
import org.iimsa.hub_service.hub.domain.service.CompanyProvider;
import org.iimsa.hub_service.hub.domain.service.RoleCheck;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
// All Services requiring MASTER role
public class HubService {

    private final HubRepository hubRepository;
    private final HubBulkRepository hubBulkRepository; // 벌크 업데이트를 위한 레포지토리
    private final RoleCheck roleCheck;
    private final AddressResolver addressResolver;
    private final CompanyProvider companyProvider;
    private final HubEvents hubEvents;
    private final EntityManager em;

    // 1. 허브 생성 (DTO 활용 및 허브 매니저 추가)
    @Transactional
    public UUID createHub(HubServiceDto.Create data) {
        Hub hub = Hub.builder()
                .name(data.getName())
                .address(data.getAddress())
                .hubManagerId(data.getHubManagerId())
                .hubManagerName(data.getHubManagerName())
                .roleCheck(roleCheck)
                .addressResolver(addressResolver)
                .events(hubEvents)
                .build();

        return hubRepository.save(hub).getId().id();
    }

    // 2. 허브 정보 수정 (이름)
    @Transactional
    public void changeHubName(UUID hubId, String newName) {
        getHub(hubId).changeName(newName, roleCheck, hubEvents);
    }

    // 3. 허브 정보 수정 (주소)
    @Transactional
    public void changeHubAddress(UUID hubId, String address) {
        getHub(hubId).changeAddress(address, addressResolver, roleCheck, hubEvents);
    }

    // 4. 허브 담당자 변경
    @Transactional
    public void changeHubManager(UUID hubId, UUID hubManagerId, String hubManagerName) {
        getHub(hubId).changeHubManager(hubManagerId, hubManagerName, roleCheck, hubEvents);
    }

    // 5. 허브 소속 상품(HubProduct) 추가
    @Transactional
    public void addProductToHub(UUID hubId, int stock, UUID companyId) {
        Hub hub = getHub(hubId);
        hub.addProduct(stock, companyId, companyProvider, roleCheck);
    }

    // 6. 허브 소속 상품(HubProduct) 삭제
    @Transactional
    public void removeProductFromHub(UUID hubId, UUID productId) {
        Hub hub = getHub(hubId);
        hub.removeProduct(ProductId.of(productId), roleCheck);
    }

    // 7. 허브 삭제
    @Transactional
    public void deleteHub(UUID hubId) {
        getHub(hubId).delete(roleCheck, hubEvents);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncCompanyInfo(Company company) {
        long updatedCount = hubBulkRepository.bulkUpdateCompanyInfo(company);
        em.clear();

        log.info("[BulkUpdate] Company ID: {} - {} hub products updated", company.getId(), updatedCount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncProductInfo(HubProduct hubProduct) {
        long updatedCount = hubBulkRepository.bulkUpdateProductInfo(hubProduct);
        em.clear();

        log.info("[BulkUpdate] Product Code: {} - {} hub products updated", hubProduct.getDetail().getName(), updatedCount);
    }

    private Hub getHub(UUID hubId) {
        return hubRepository.findActiveById(hubId)
                .orElseThrow(() -> new HubNotFoundException(hubId));
    }

    private String getMasterId() {
        return SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new UnAuthorizedException("관리자 인증 정보가 유효하지 않습니다."));
    }
}
