package org.iimsa.hub_service.hub.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.application.query.HubQueryService;
import org.iimsa.hub_service.hub.application.service.HubService;
import org.iimsa.hub_service.hub.presentation.dto.request.CreateHubProductRequestDto;
import org.iimsa.hub_service.hub.presentation.dto.request.CreateHubRequestDto;
import org.iimsa.hub_service.hub.presentation.dto.request.PatchHubRequestDto;
import org.iimsa.hub_service.hub.presentation.dto.request.SearchHubRequestDto;
import org.iimsa.hub_service.hub.presentation.dto.response.CreateHubResponseDto;
import org.iimsa.hub_service.hub.presentation.dto.response.GetHubResponseDto;
import org.iimsa.hub_service.hub.presentation.dto.response.GetHubResponseDto.Info;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hubs")
@Tag(name = "허브 API", description = "허브 생성 및 관리, 조회 API")
public class HubController {

    private final HubService hubService;
    private final HubQueryService hubQueryService;

    // create
    @Operation(summary = "허브 생성", description = "MASTER 권한 사용자가 새로운 허브를 등록합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateHubResponseDto.Create createHub(@RequestBody CreateHubRequestDto.Create requestDto) {
        UUID hubId = hubService.createHub(requestDto.toDto());
        return new CreateHubResponseDto.Create(hubId);
    }

    @Operation(summary = "허브에 상품 추가", description = "특정 허브에 업체의 상품을 보관(추가)합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping("/{hubId}/hubProducts")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProductToHub(
            @PathVariable UUID hubId,
            @RequestBody CreateHubProductRequestDto requestDto) {
        hubService.addProductToHub(hubId, requestDto.getStock(), requestDto.getCompanyId());
    }

    // read
    @Operation(summary = "허브 단건 상세 조회", description = "ID를 통해 특정 허브의 상세 정보를 조회합니다.")
    @GetMapping("/{hubId}")
    public GetHubResponseDto.Info getHub(
            @Parameter(description = "조회할 허브 ID", required = true) @PathVariable UUID hubId) {
        return hubQueryService.getHub(hubId);
    }

    @Operation(summary = "전체 허브 목록 조건 검색", description = "조건(허브명, 주소, 담당자 등)과 페이징을 통해 허브 목록을 통합 검색합니다.")
    @GetMapping
    @PageableAsQueryParam
    public Page<Info> searchHubs(
            @ParameterObject SearchHubRequestDto.Search request,
            @Parameter(hidden = true) Pageable pageable) {
        return hubQueryService.searchHubs(request.toDomainDto(), pageable);
    }

    @Operation(summary = "특정 담당자가 관리하는 허브 목록 조회", description = "지정된 허브 매니저(Manager) ID가 관리하는 허브 목록을 조회합니다.")
    @GetMapping("/hubManagers/{hubManagerId}")
    @PageableAsQueryParam
    public Page<GetHubResponseDto.Info> searchHubsByManager(
            @Parameter(description = "허브 담당자 ID", required = true) @PathVariable UUID hubManagerId,
            @Parameter(hidden = true) Pageable pageable) {
        return hubQueryService.searchHubsByManager(hubManagerId, pageable);
    }

    @Operation(summary = "특정 업체가 속한 허브 목록 조회", description = "해당 업체의 상품을 보관 중인 허브 목록을 조회합니다.")
    @GetMapping("/companies/{companyId}")
    @PageableAsQueryParam
    public Page<GetHubResponseDto.Info> searchHubsByCompany(
            @Parameter(description = "업체 ID", required = true) @PathVariable UUID companyId,
            @Parameter(hidden = true) Pageable pageable) {
        return hubQueryService.searchHubsByCompany(companyId, pageable);
    }

    // update
    @Operation(summary = "허브 이름 수정", description = "허브의 이름을 변경합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{hubId}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeHubName(
            @PathVariable UUID hubId,
            @RequestBody PatchHubRequestDto.ChangeHubName request) {
        hubService.changeHubName(hubId, request.getName());
    }

    @Operation(summary = "허브 주소 수정", description = "허브의 주소를 변경합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{hubId}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeHubAddress(
            @PathVariable UUID hubId,
            @RequestBody PatchHubRequestDto.ChangeHubAddress request) {
        hubService.changeHubAddress(hubId, request.getAddress());
    }

    @Operation(summary = "허브 담당자 변경", description = "허브의 매니저를 변경합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @PatchMapping("/{hubId}/hubManager")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeHubManager(
            @PathVariable UUID hubId,
            @RequestBody PatchHubRequestDto.ChangeHubManager request) {
        hubService.changeHubManager(hubId, request.getHubManagerId(), request.getHubManagerName());
    }

    // delete
    @Operation(summary = "허브 상품 삭제", description = "특정 허브에 보관 중인 상품 데이터를 삭제합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{hubId}/hubProducts/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProductFromHub(
            @PathVariable UUID hubId,
            @PathVariable UUID productId) {
        hubService.removeProductFromHub(hubId, productId);
    }

    @Operation(summary = "허브 삭제", description = "등록된 허브를 삭제(논리적 삭제)합니다.")
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{hubId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHub(@PathVariable UUID hubId) {
        hubService.deleteHub(hubId);
    }
}
