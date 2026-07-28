package org.iimsa.hub_service.hubroute.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ticketing.common.response.CommonResponse;
import jakarta.validation.Valid;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery.Algorithm;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.dto.query.ListHubRouteQuery;
import org.iimsa.hub_service.hubroute.application.service.HubRouteApplicationService;
import org.iimsa.hub_service.hubroute.domain.model.HubRoutePath;
import org.iimsa.hub_service.hubroute.presentation.dto.request.CreateHubRouteRequest;
import org.iimsa.hub_service.hubroute.presentation.dto.request.UpdateHubRouteRequest;
import org.iimsa.hub_service.hubroute.presentation.dto.response.HubRoutePathResponse;
import org.iimsa.hub_service.hubroute.presentation.dto.response.HubRouteResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "HubRoute", description = "허브 경로 관리 API")
@RestController
@RequestMapping("/api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private final HubRouteApplicationService hubRouteApplicationService;

    @Operation(summary = "허브 경로 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<HubRouteResponse> createHubRoute(
            @Valid @RequestBody CreateHubRouteRequest request
    ) {
        HubRouteResponse response = HubRouteResponse.from(
                hubRouteApplicationService.createHubRoute(request.toCommand())
        );
        return CommonResponse.success("허브 경로가 생성되었습니다.", response);
    }

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

    @Operation(summary = "허브 경로 수정", description = "estimatedDistance, estimatedDuration 부분 수정 가능 (PATCH)")
    @PatchMapping("/{hubRouteId}")
    public CommonResponse<HubRouteResponse> updateHubRoute(
            @PathVariable UUID hubRouteId,
            @Valid @RequestBody UpdateHubRouteRequest request
    ) {
        HubRouteResponse response = HubRouteResponse.from(
                hubRouteApplicationService.updateHubRoute(hubRouteId, request.toCommand())
        );
        return CommonResponse.success("허브 경로가 수정되었습니다.", response);
    }

    @Operation(summary = "허브 경로 논리 삭제")
    @DeleteMapping("/{hubRouteId}")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<HubRouteResponse> deleteHubRoute(
            @PathVariable UUID hubRouteId
    ) {
        HubRouteResponse response = HubRouteResponse.from(
                hubRouteApplicationService.deleteHubRoute(hubRouteId)
        );
        return CommonResponse.success("허브 경로가 삭제되었습니다.", response);
    }

    @Operation(
            summary = "출발 허브 → 도착 허브 최적 전체 경로 조회",
            description = """
                    Hub 서비스가 배차 시 Feign으로 호출합니다.
                    algorithm 파라미터로 탐색 알고리즘을 선택할 수 있습니다 (기본값: ASTAR).
                    응답 헤더 X-Nodes-Explored 에 알고리즘이 탐색한 노드 수가 포함됩니다.
                    """
    )
    @GetMapping("/path")
    public ResponseEntity<CommonResponse<HubRoutePathResponse>> findOptimalRoute(
            @RequestParam UUID originHubId,
            @RequestParam UUID destinationHubId,
            @RequestParam(defaultValue = "ASTAR") Algorithm algorithm
    ) {
        HubRoutePath path = hubRouteApplicationService.findOptimalRoute(
                new FindHubRoutePathQuery(originHubId, destinationHubId, algorithm)
        );
        HubRoutePathResponse response = HubRoutePathResponse.from(path);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Nodes-Explored", String.valueOf(path.nodesExplored()));
        headers.add("X-Algorithm", algorithm.name());

        return ResponseEntity.ok()
                .headers(headers)
                .body(CommonResponse.success(response));
    }
}
