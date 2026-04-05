package org.iimsa.hub_service.hubroute.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepository {

    private final JpaHubRouteRepository jpaHubRouteRepository;

    @Override
    public HubRoute save(HubRoute hubRoute) {
        return jpaHubRouteRepository.save(hubRoute);
    }

    @Override
    public Optional<HubRoute> findActiveById(UUID id) {
        return jpaHubRouteRepository.findActiveById(id);
    }

    @Override
    public Page<HubRoute> findAllActive(Pageable pageable) {
        return jpaHubRouteRepository.findAllActive(pageable);
    }

    @Override
    public Page<HubRoute> findAllActiveByFromHubId(UUID fromHubId, Pageable pageable) {
        return jpaHubRouteRepository.findAllActiveByFromHubId(fromHubId, pageable);
    }

    @Override
    public List<HubRoute> findAllActive() {
        return jpaHubRouteRepository.findAllActive();
    }

    @Override
    public boolean existsByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId) {
        return jpaHubRouteRepository.existsByFromHubIdAndToHubId(fromHubId, toHubId);
    }
}
