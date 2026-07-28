package org.iimsa.hub_service.hub.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.iimsa.hub_service.hub.application.dto.HubServiceDto;

@NoArgsConstructor
public class CreateHubRequestDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Create {
        @NotBlank
        private String hubName;
        @NotNull
        private String address;
        @NotNull
        private UUID hubManagerId;

        public HubServiceDto.Create toDto() {
            return HubServiceDto.Create.builder()
                    .name(this.hubName)
                    .address(this.address)
                    .hubManagerId(this.hubManagerId)
                    .build();
        }
    }
}
