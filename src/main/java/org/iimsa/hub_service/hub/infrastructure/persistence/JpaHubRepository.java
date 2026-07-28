package org.iimsa.hub_service.hub.infrastructure.persistence;

import java.util.Optional;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaHubRepository extends JpaRepository<Hub, HubId> {

    @Query("SELECT h FROM Hub h WHERE h.id = :id AND h.deletedAt IS NULL")
    Optional<Hub> findActiveById(@Param("id") HubId id);

}
