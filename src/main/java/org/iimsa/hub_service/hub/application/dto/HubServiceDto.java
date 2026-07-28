package org.iimsa.hub_service.hub.application.dto;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HubServiceDto {

    @Getter
    @Builder
    public static class Create {
        private final String name;
        private final String address;

        private final UUID hubManagerId;
        private final String hubManagerName;
    }
}
