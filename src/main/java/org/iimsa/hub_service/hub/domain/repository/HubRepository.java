package org.iimsa.hub_service.hub.domain.repository;

import org.iimsa.hub_service.hub.domain.model.Hub;

import java.util.Optional;
import java.util.UUID;

public interface HubRepository {

    Hub save(Hub hub);

    Optional<Hub> findActiveById(UUID id);

    boolean existsById(UUID id);
}
