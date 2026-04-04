package org.iimsa.hub_service.hubroute.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.service.HubRouteApplicationService;
import org.iimsa.hub_service.hubroute.presentation.dto.response.HubRouteResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "HubRoute", description = "허브 경로 관리 API")
@RestController
@RequestMapping("/api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private final HubRouteApplicationService hubRouteApplicationService;

    @Operation(summary = "허브 경로 단건 조회")
    @GetMapping("/{hubRouteId}")
    public CommonResponse<HubRouteResponse> findHubRoute(
            @PathVariable UUID hubRouteId
    ) {
        HubRouteResponse response = HubRouteResponse.from(
                hubRouteApplicationService.findHubRoute(new FindHubRouteQuery(hubRouteId))
        );
        return CommonResponse.success(response);
    }

    @Operation(summary = "허브 경로 목록 조회", description = "fromHubId 파라미터 없으면 전체 조회")
    @GetMapping
    public CommonResponse<Page<HubRouteResponse>> listHubRoutes(
            @RequestParam(required = false) UUID fromHubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HubRouteResponse> response = hubRouteApplicationService
                .listHubRoutes(new ListHubRouteQuery(fromHubId, page, size))
                .map(HubRouteResponse::from);
        return CommonResponse.success(response);
    }
}
