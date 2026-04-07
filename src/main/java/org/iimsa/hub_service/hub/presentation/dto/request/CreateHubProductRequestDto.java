package org.iimsa.hub_service.hub.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreateHubProductRequestDto {

    @NotNull(message = "상품 ID(productId)는 필수 입력값입니다.")
    private UUID productId;

    @NotNull(message = "업체 ID(companyId)는 필수 입력값입니다.")
    private UUID companyId;

    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private int stock;

}
