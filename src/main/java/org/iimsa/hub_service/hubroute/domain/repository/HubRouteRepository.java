package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository {

    HubRoute save(HubRoute hubRoute);

    Optional<HubRoute> findActiveById(UUID id);

    Page<HubRoute> findAllActive(Pageable pageable);

    /** 스케줄러 전용 — 페이징 없이 전체 활성 경로 조회 */
    List<HubRoute> findAllActive();

    Page<HubRoute> findAllActiveByFromHubId(UUID fromHubId, Pageable pageable);

    boolean existsByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);
}
