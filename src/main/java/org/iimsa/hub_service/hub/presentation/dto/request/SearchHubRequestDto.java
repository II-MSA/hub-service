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

        // 허브 담당자 ID (단일 검색으로 통일)
        private UUID hubManagerId;

        // 허브 담당자 이름 (Hub.hubManager.hubManagerName)
        private String hubManagerName;

        // 소속/입점 업체 ID 목록 (HubProduct.company.companyId)
        private List<UUID> companyIds;

        // 소속/입점 업체 이름 (추가됨)
        private String companyName;

        // RequestDto -> DomainDto 변환 로직
        public HubQueryDto.Search toDomainDto() {
            return HubQueryDto.Search.builder()
                    .hubIds(hubIds != null
                            ? hubIds.stream().map(HubId::of).collect(Collectors.toList())
                            : null)
                    .hubName(this.name)
                    .address(this.address)
                    .hubManagerId(this.hubManagerId)
                    .hubManagerName(this.hubManagerName)
                    .companyIds(this.companyIds)
                    .companyName(this.companyName)
                    .build();
        }
    }
}
