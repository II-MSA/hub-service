package org.iimsa.hub_service.hubroute.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hubroute.domain.model.HubRouteHistory;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRouteHistoryRepositoryImpl implements HubRouteHistoryRepository {

    private final JpaHubRouteHistoryRepository jpaRepository;

    @Override
    public HubRouteHistory save(HubRouteHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public Optional<Double> findAverageDuration(UUID fromHubId, UUID toHubId) {
        return jpaRepository.findAverageDuration(fromHubId, toHubId);
    }
}
