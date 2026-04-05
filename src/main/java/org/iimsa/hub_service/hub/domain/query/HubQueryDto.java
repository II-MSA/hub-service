package org.iimsa.hub_service.hub.domain.query;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.HubId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HubQueryDto {

    @Getter
    @Builder
    public static class Search {
        private List<HubId> hubIds;
        private String name;
        private String address;
        private List<UUID> hubManagerIds;
        private String hubManagerName;
        private List<UUID> companyIds;
    }

}
