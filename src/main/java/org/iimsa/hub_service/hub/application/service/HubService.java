package org.iimsa.hub_service.hub.application.service;

import org.iimsa.hub_service.hub.application.dto.HubServiceDto;
import org.iimsa.hub_service.hub.domain.event.payload.OrderCreatedPayload;
import org.iimsa.hub_service.hub.domain.model.Company;
import org.iimsa.hub_service.hub.domain.model.HubProduct;

import java.util.UUID;

public interface HubService {

    UUID createHub(HubServiceDto.Create data);

    void changeHubName(UUID hubId, String newName);

    void changeHubAddress(UUID hubId, String address);

    void changeHubManager(UUID hubId, UUID hubManagerId, String hubManagerName);

    void addProductToHub(UUID hubId, int stock, UUID companyId);

    void removeProductFromHub(UUID hubId, UUID productId);

    void deleteHub(UUID hubId);

    void syncCompanyInfo(Company company);

    void removeProductStock(UUID hubId, UUID productId, int quantity);

    void syncProductInfo(HubProduct hubProduct);

    void processOrderCreatedEvent(OrderCreatedPayload event);
}
