package org.iimsa.hub_service.hub.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.iimsa.hub_service.hub.domain.query.HubQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubQueryRepository {

    // 1. 허브 단건 조회
    Optional<Hub> findById(HubId hubId);

    // 2. 통합 검색
    Page<Hub> search(HubQueryDto.Search search, Pageable pageable);

    // 3. 허브 담당자별 조회
    Page<Hub> findAllByHubManagerId(UUID hubManagerId, Pageable pageable);

    // 4. 업체별 허브 조회
    Page<Hub> findAllByCompanyId(UUID companyId, Pageable pageable);

    // 5. 상품 중복 검증
    boolean isDuplicatedItem(UUID companyId);
}
