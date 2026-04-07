package org.iimsa.hub_service.hub.presentation.dto.response;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.Hub;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetHubResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Info {
        private UUID hubId;
        private String hubName;

        // Address 정보
        private String address;
        private Double latitude;
        private Double longitude;

        // HubManager 정보
        private UUID hubManagerId;
        private String hubManagerName;

        public static Info from(Hub hub) {
            return Info.builder()
                    .hubId(hub.getId().id())
                    .hubName(hub.getName())

                    .address(hub.getAddress() != null ? hub.getAddress().getAddress() : null)
                    .latitude(hub.getAddress() != null ? hub.getAddress().getLatitude() : null)
                    .longitude(hub.getAddress() != null ? hub.getAddress().getLongitude() : null)

                    .hubManagerId(hub.getHubManager() != null ? hub.getHubManager().getHubManagerId() : null)
                    .hubManagerName(hub.getHubManager() != null ? hub.getHubManager().getHubManagerName() : null)
                    .build();
        }
    }
}
