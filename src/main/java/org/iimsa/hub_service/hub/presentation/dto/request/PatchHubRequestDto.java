package org.iimsa.hub_service.hub.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchHubRequestDto {

    @Data
    public static class ChangeHubName {
        @NotNull
        private String name;
    }

    @Data
    public static class ChangeHubAddress {
        @NotNull
        private String address;
    }

    @Data
    public static class ChangeHubManager {
        @NotNull
        private UUID hubManagerId;
        @NotNull
        private String hubManagerName;
    }
}
