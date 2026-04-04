package org.iimsa.hub_service.hubroute.infrastructure.persistence;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaHubRouteRepository extends JpaRepository<HubRoute, UUID>,
        QuerydslPredicateExecutor<HubRoute> {

    @Query("SELECT r FROM HubRoute r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<HubRoute> findActiveById(@Param("id") UUID id);

    @Query("SELECT r FROM HubRoute r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<HubRoute> findAllActive(Pageable pageable);

    @Query("SELECT r FROM HubRoute r WHERE r.fromHubId = :fromHubId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<HubRoute> findAllActiveByFromHubId(@Param("fromHubId") UUID fromHubId, Pageable pageable);

    boolean existsByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);
}
