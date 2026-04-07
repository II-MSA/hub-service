package org.iimsa.hub_service.hub.application.query;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.domain.exception.HubNotFoundException;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.iimsa.hub_service.hub.domain.query.HubQueryDto;
import org.iimsa.hub_service.hub.domain.repository.HubQueryRepository;
import org.iimsa.hub_service.hub.presentation.dto.response.GetHubResponseDto;
import org.iimsa.hub_service.hub.presentation.dto.response.GetHubResponseDto.Info;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubQueryService {

    private final HubQueryRepository hubQueryRepository;

    // 1. 허브 단건 조회
    public GetHubResponseDto.Info getHub(UUID hubId) {
        return hubQueryRepository.findById(HubId.of(hubId))
                .map(GetHubResponseDto.Info::from)
                .orElseThrow(() -> new HubNotFoundException(hubId));
    }

    // 2. 조건별 허브 목록 통합 검색 (Search Dto 활용 - 허브명, 담당자, 업체명, 상품명 등)
    public Page<Info> searchHubs(HubQueryDto.Search search, Pageable pageable) {
        Pageable validatedPageable = validatePageable(pageable);

        return hubQueryRepository.search(search, validatedPageable)
                .map(GetHubResponseDto.Info::from);
    }

    // 3. 특정 담당자(HubManager)가 관리하는 허브 목록 조회
    public Page<GetHubResponseDto.Info> searchHubsByManager(UUID hubManagerId, Pageable pageable) {
        Pageable validatedPageable = validatePageable(pageable);

        return hubQueryRepository.findAllByHubManagerId(hubManagerId, validatedPageable)
                .map(GetHubResponseDto.Info::from);
    }

    // 4. 특정 업체의 상품을 보관 중인 허브 목록 조회 (HubProduct 내 Company 조회)
    public Page<GetHubResponseDto.Info> searchHubsByCompany(UUID companyId, Pageable pageable) {
        Pageable validatedPageable = validatePageable(pageable);

        return hubQueryRepository.findAllByCompanyId(companyId, validatedPageable)
                .map(GetHubResponseDto.Info::from);
    }

    private Pageable validatePageable(Pageable pageable) {
        int pageSize = pageable.getPageSize();

        if (pageSize != 10 && pageSize != 30 && pageSize != 50) {
            return PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }

        return pageable;
    }

}
