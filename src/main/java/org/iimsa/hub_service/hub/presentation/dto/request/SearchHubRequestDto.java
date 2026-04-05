package org.iimsa.hub_service.hub.presentation.dto.request;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.iimsa.hub_service.hub.domain.query.HubQueryDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchHubRequestDto {

    @Data
    public static class Search {

        // 허브 ID 목록
        private List<UUID> hubIds;

        // 허브 이름 (Hub.name)
        private String name;

        // 허브 주소 (Hub.address.address)
        private String address;

        // 허브 담당자 ID 목록 (Hub.hubManager.hubManagerId)
        private List<UUID> hubManagerIds;

        // 허브 담당자 이름 (Hub.hubManager.hubManagerName)
        private String hubManagerName;

        // 소속/입점 업체 ID 목록 (HubProduct.company.companyId)
        // HubQueryRepository의 findAllByCompanyId 메서드를 참고하여 다중 검색용으로 추가
        private List<UUID> companyIds;

        public HubQueryDto.Search toDomainDto() {
            return HubQueryDto.Search.builder()
                    .hubIds(hubIds != null
                            ? hubIds.stream().map(HubId::of).collect(Collectors.toList())
                            : null)
                    .name(name)
                    .address(address)
                    .hubManagerIds(hubManagerIds)
                    .hubManagerName(hubManagerName)
                    .companyIds(companyIds)
                    .build();
        }
    }
}
