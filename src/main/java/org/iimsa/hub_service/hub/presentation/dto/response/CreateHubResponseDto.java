package org.iimsa.hub_service.hub.presentation.dto.response;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateHubResponseDto {
    @AllArgsConstructor
    public static class Create {
        public UUID id;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Info {
        private UUID hubId;
        private String hubName;
        private String address;
        private HubManager hubManager;

        public static Info from(Hub hub) {

            return Info.builder()
                    .hubId(hub.getId().id())
                    .hubName(hub.getName())
                    .address(hub.getAddress().getAddress())
                    .hubManager(hub.getHubManager())
                    .build();
        }
    }

}
