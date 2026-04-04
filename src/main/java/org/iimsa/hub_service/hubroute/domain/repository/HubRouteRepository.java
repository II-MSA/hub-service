package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository {

    HubRoute save(HubRoute hubRoute);

    Optional<HubRoute> findActiveById(UUID id);

    Page<HubRoute> findAllActive(Pageable pageable);

    Page<HubRoute> findAllActiveByFromHubId(UUID fromHubId, Pageable pageable);

    boolean existsByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);
}
