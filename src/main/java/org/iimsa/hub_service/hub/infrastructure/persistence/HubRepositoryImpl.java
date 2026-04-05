package org.iimsa.hub_service.hub.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.repository.HubRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepository {

    private final JpaHubRepository jpaHubRepository;

    @Override
    public Hub save(Hub hub) {
        return jpaHubRepository.save(hub);
    }

    @Override
    public Optional<Hub> findActiveById(UUID id) {
        return jpaHubRepository.findActiveById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaHubRepository.existsById(id);
    }
}
