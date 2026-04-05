package org.iimsa.hub_service.hub.infrastructure.persistence;

import org.iimsa.hub_service.hub.domain.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaHubRepository extends JpaRepository<Hub, UUID> {

    @Query("SELECT h FROM Hub h WHERE h.id = :id AND h.deletedAt IS NULL")
    Optional<Hub> findActiveById(@Param("id") UUID id);
}
